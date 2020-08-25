package com.nokia.netguard.adapter.cli;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.LinkedList;
import java.util.List;

import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.CLICommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.callback.Callback;

/**
 * Class to execute Command objects. Currently only CLICommand is supported
 *
 * @author adskoczy
 *
 */
public class Executor {

	private static final Integer DEFAULT_TIMEOUT = 60000;
	private static final String DEFAULT_PROMPT = "\\$$|\\$ $|#$|# $";
	private static final String SUDO_PROMPT = "\\[sudo\\] password for .+?:";
	private static final String DEFAULT_MASK = "********";
	private static final String SUDO_PREFIX = "sudo ";
	private static final String PRINT_EXIT_CODE_SUFFIX = ";echo $?";

	private BaseCommand base;
	private String sudoPassword;
	private String prompt;
	private Integer timeout;
	private String mask = DEFAULT_MASK;
	private String newLineCharacter;

	public Executor(BaseCommand base) throws RequestFailure {
		this.base = base;
		sudoPassword = base.getAuthenticationArguments("CLI").getPassword();
		initializeParametersFromConfigFile();
	}

	private void initializeParametersFromConfigFile() throws RequestFailure {
		AdapterConfiguration config = new AdapterConfiguration(base);
		prompt = config.getStringValue("interfaceType.cli.initialNEPrompt", DEFAULT_PROMPT);
		timeout = config.getIntegerValue("interfaceType.cli.defaultTimeoutInMilliseconds", DEFAULT_TIMEOUT);
		newLineCharacter = config.getNeLineTermination();
		validateParameters();
	}

	private void validateParameters() throws RequestFailure {
		if (prompt == null) {
			throw new RequestFailure("Prompt is null");
		}
		if (timeout == null) {
			throw new RequestFailure("Timeout is null");
		}
		if (newLineCharacter == null) {
			throw new RequestFailure("New line character is null");
		}
	}

	/**
	 * Execute command.
	 * @param command Command object to execute
	 * @return Result of command execution
	 * @throws RequestFailure
	 */
	public ExecutionResult execute(Command command) throws RequestFailure {
		validateCommand(command);
		boolean retrieveExitCode = command.shouldRetrieveExitCode();
		boolean extractExitCode = command.shouldRetrieveExitCode();
		List<Callback> additionalCallbacks = new LinkedList<>();
		return executeSingleCommand(command, retrieveExitCode, extractExitCode, additionalCallbacks);
	}

	/**
	 * Execute interactive command.
	 * @param interactiveCommand Interactive command to execute
	 * @return Result of interactive command execution
	 * @throws RequestFailure
	 */
	public ExecutionResult execute(InteractiveCommand interactiveCommand) throws RequestFailure {
		if (interactiveCommand == null || interactiveCommand.isEmpty()) {
			throw new RequestFailure("Unable to execute empty interactive command.");
		}

		int firstInSequence = 0;
		int lastInSequence = interactiveCommand.size() - 1;
		ExecutionResult executionResult = new ExecutionResult(EMPTY, EMPTY);
		for (int i = 0; i < interactiveCommand.size(); i++) {
			Command command = interactiveCommand.get(i);
			validateCommand(command);
			boolean retrieveExitCode = (i == firstInSequence && (command.shouldRetrieveExitCode() || interactiveCommand.shouldRetrieveExitCode()));
			boolean extractExitCode = (i == lastInSequence && (command.shouldRetrieveExitCode() || interactiveCommand.shouldRetrieveExitCode()));
			List<Callback> additionalCallbacks = interactiveCommand.getCallbacks();
			ExecutionResult singleExecutionResult = executeSingleCommand(command, retrieveExitCode, extractExitCode, additionalCallbacks);
			executionResult.setOutput((executionResult.getOutput() + newLineCharacter + singleExecutionResult.getOutput()).trim());
			executionResult.setCleanOutput((executionResult.getCleanOutput() + newLineCharacter + singleExecutionResult.getCleanOutput()).trim());
			if (i == lastInSequence) {
				executionResult.setExitCode(singleExecutionResult.getExitCode());
			}
		}
		return executionResult;
	}

	private ExecutionResult executeSingleCommand(Command command, boolean retrieveExitCode, boolean extractExitCode, List<Callback> additionalCallbacks)
			throws RequestFailure {
		String commandName = prepareCommandName(command);
		String commandText = prepareCommandText(command, retrieveExitCode);
		Integer commandTimeout = prepareCommandTimeout(command);
		String commandPrompt = prepareCommandPrompt(command);
		ExecutionResult executionResult = sendCommand(command, commandName, commandText, commandTimeout, commandPrompt);
		extractExitCode(executionResult, extractExitCode);
		executeCallbacks(command, executionResult, additionalCallbacks);
		return executionResult;
	}

	private void validateCommand(Command command) throws RequestFailure {
		if (command == null) {
			throw new RequestFailure("Command object is null");
		}
		if (command.isEscalated() && sudoPassword == null && command.getEscalatePassword() == null) {
			throw new RequestFailure("Password for sudo command is not set.");
		}
		if (command.getCommand() == null) {
			throw new RequestFailure("Command string is null or empty");
		}
	}

	private String prepareCommandName(Command command) {
		String commandName = command.getCommand().trim();
		if (command.getName() != null) {
			commandName = command.getName();
		}
		return commandName;
	}

	private String prepareCommandText(Command command, boolean retrieveExitCode) {
		String commandText = command.isTrimmed() ? command.getCommand().trim() : command.getCommand();
		if (command.isEscalated() && !commandText.startsWith(SUDO_PREFIX)) {
			commandText = SUDO_PREFIX + commandText;
		}
		if (retrieveExitCode) {
			commandText += PRINT_EXIT_CODE_SUFFIX;
		}
		if (command.isAutoNewLined()) {
			commandText += newLineCharacter;
		}
		return commandText;
	}

	private Integer prepareCommandTimeout(Command command) {
        Integer commandTimeout = timeout;
        if (command.getTimeout() != null && command.getTimeout() > 0) {
            commandTimeout = command.getTimeout();
        }
        return commandTimeout;
    }

	private String prepareCommandPrompt(Command command) {
		String commandPrompt = prompt;
		if (command.getPrompt() != null) {
			commandPrompt += "|" + command.getPrompt();
		}
		if (command.isEscalated()) {
			commandPrompt += "|" + SUDO_PROMPT;
		}
		return commandPrompt;
	}

	private ExecutionResult sendCommand(Command command, String commandName, String commandText, Integer commandTimeout,
										String commandPrompt) throws RequestFailure {
		CLICommand commandObject = new CLICommand(base, commandName, commandText,
				(command.isMasked() ? mask : commandText), commandPrompt, commandTimeout);
		String output = commandObject.send();
		String cleanOutput = commandObject.removeEchoAndPrompt();
		if (output.contains("[sudo]")) {
			commandObject = new CLICommand(base, commandName, (command.getEscalatePassword() != null ?
					command.getEscalatePassword() : sudoPassword) + newLineCharacter, mask, commandPrompt, commandTimeout);
			output += commandObject.send();
			cleanOutput += commandObject.removeEchoAndPrompt();
		}
		return new ExecutionResult(output, cleanOutput.trim());
	}

	private void extractExitCode(ExecutionResult executionResult, boolean extractExitCode) throws RequestFailure {
		if (!extractExitCode) {
			return;
		}
		String output = executionResult.getOutput();
		String exitCodeString = extractExitCodeString(output);
		executionResult.setExitCode(parseExitCodeString(exitCodeString));
		executionResult.setCleanOutput(removeExitCodeFromOutput(executionResult.getCleanOutput()));
	}

	private String extractExitCodeString(String output) throws RequestFailure {
		int newLineBeforePrompt = output.lastIndexOf(newLineCharacter);
		if (newLineBeforePrompt == -1) {
			throw new RequestFailure("Output is incomplete, contains only single line.");
		}
		String outputWithoutPrompt = output.substring(0, newLineBeforePrompt);
		int newLineBeforeExitCode = outputWithoutPrompt.lastIndexOf(newLineCharacter);
		return (newLineBeforeExitCode == -1 ? outputWithoutPrompt : outputWithoutPrompt.substring(newLineBeforeExitCode)).trim();
	}

	private Integer parseExitCodeString(String exitCodeString) throws RequestFailure {
		Integer exitCode;
		try {
			exitCode = Integer.parseInt(exitCodeString);
		} catch (NumberFormatException exception) {
			throw new RequestFailure("Cannot parse exit code of executed command to a number (string: '" + exitCodeString + "')");
		}
		return exitCode;
	}

	private String removeExitCodeFromOutput(String cleanOutput) {
		Integer lastNewLine = cleanOutput.lastIndexOf(newLineCharacter);
		return (lastNewLine == -1 ? EMPTY : cleanOutput.substring(0, lastNewLine));
	}

	private void executeCallbacks(Command command, ExecutionResult executionResult, List<Callback> additionalCallbacks) throws RequestFailure {
		for (Callback callback : command.getCallbacks()) {
			callback.call(executionResult);
		}
		for (Callback callback : additionalCallbacks) {
			callback.call(executionResult);
		}
	}
}
