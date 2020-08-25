package com.nokia.netguard.adapter.cli;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.nokia.netguard.adapter.cli.callback.Callback;

/**
 * Class which keep specific command settings
 *
 * @author adskoczy
 *
 */
public class Command {
	private String name;
	private String commandString;
	private List<Callback> callbacks = new LinkedList<>();
	private boolean isEscalated = false;
	private boolean isMasked = false;
	private boolean retrieveExitCode = false;
	private boolean trim = true;
	private boolean autoNewLine = true;
	private String prompt;
	private String escalatePassword;
	private Integer timeout;

	/**
	 * Create new Command object with empty command string
	 * @return new Command instance
	 */
	public static Command create() {
		return Command.create("");
	}

	/**
	 * Create new Command object and set command string
	 *
	 * @param command - non-null command string
	 * @param arguments optional command arguments strings
	 * @return new Command instance
	 * @throws IllegalArgumentException if the command string is null
	 */
	public static Command create(String command, String... arguments) {
		if (command == null) {
			throw new IllegalArgumentException("The command string cannot be null");
		}
		ArrayList<String> commandElements = new ArrayList<>();
		commandElements.add(command);
		commandElements.addAll(asList(arguments));
		String s = join(commandElements, " ");
		return new Command().setCommand(s);
	}

	/**
	 * Get command name
	 * @return command name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set command name
	 * @param name command name
	 * @return current Command instance
	 */
	public Command setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get command string
	 * @return command string
	 */
	public String getCommand() {
		return commandString;
	}

	/**
	 * Set command string
	 *
	 * @param commandString
	 *            command string to call, without '\n'
	 * @return current Command instance
	 */
	public Command setCommand(String commandString) {
		this.commandString = commandString;
		return this;
	}

	public List<Callback> getCallbacks() {
		return callbacks;
	}

	/**
	 * 
	 * @param callback
	 *            Callback to be called when command execution is ready. Multiple callbacks may be added, they will be called in the same order as they have been added.
	 * @return current Command instance
	 */
	public Command addCallback(Callback callback) {
		callbacks.add(callback);
		return this;
	}

	/**
	 * 
	 * @return true if command will be executed as sudo
	 */
	public boolean isEscalated() {
		return isEscalated;
	}

	/**
	 * By calling this command Command object is set to be executed with sudo prefix
	 *
	 * @return current Command instance
	 */
	public Command escalateCommand() {
		this.isEscalated = true;
		return this;
	}

	/**
	 * By calling this command Command object is set to be executed with sudo prefix
	 * and custom password
	 * 
	 * @param password custom password for escalate command
	 * 
	 * @return current Command instance
	 */
	public Command escalateCommand(String password) {
		escalateCommand();

		this.escalatePassword = password;
		return this;
	}

	public String getEscalatePassword() {
		return escalatePassword;
	}

	/**
	 * 
	 * @return true if command will be masked in logs
	 */
	public boolean isMasked() {
		return isMasked;
	}

	/**
	 * By calling this command Command object is set to mask command string in logs
	 *
	 * @return current Command instance
	 */
	public Command setMasked() {
		this.isMasked = true;
		return this;
	}

	/**
	 * By calling this command we qualify command to have exit code retrieved after execution.
	 */
	public Command retrieveExitCode() {
		this.retrieveExitCode = true;
		return this;
	}

	/**
	 * Returns information whether command should have exit code retrieved after execution.
	 * @return true if command will have exit code retrieved, false otherwise.
	 */
	public boolean shouldRetrieveExitCode() {
		return this.retrieveExitCode;
	}

	/**
	 * By calling this commend we specify that the command string will not be trimmed from whitespace/special characters.<br/>
	 * By default, the command string is trimmed with String trim() method;
	 * @return current Command instance
	 */
	public Command noTrim() {
		this.trim = false;
		return this;
	}
	
	/**
	 * Returns information whether the command string will be trimmed from whitespace/special characters before execution.
	 * @return
	 */
	public boolean isTrimmed() {
		return this.trim;
	}
	
	/**
	 * Don't automatically add new-line character at end of command string.
	 * By default, the command string is automatically postfixed with the new-line character.
	 * @return current Command instance
	 */
	public Command noAutoNewLine() {
		this.autoNewLine = false;
		return this;
	}
	
	/**
	 * Is the new-line character automatically added to the end of command string?
	 * @return
	 */
	public boolean isAutoNewLined() {
		return this.autoNewLine;
	}
	
	/**
	 * 
	 * @return prompt string
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 *
	 * @param promptRegex
	 *            prompt regex string. If method is called several times new strings
	 *            will be added as follow 'promptRegex1|promptRegex2|promptRegex3'
	 * @return current Command instance
	 */
	public Command addPrompt(String promptRegex) {
		if (promptRegex == null || promptRegex.isEmpty()) {
			return this;
		}

		if (this.prompt != null) {
			this.prompt += ("|" + promptRegex);
		} else {
			this.prompt = promptRegex;
		}
		return this;
	}

	public Command setTimeout(Integer timeout) {
		this.timeout = timeout;
		return this;
	}
}
