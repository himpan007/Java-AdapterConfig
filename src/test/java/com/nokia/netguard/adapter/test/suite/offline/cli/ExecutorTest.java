package com.nokia.netguard.adapter.test.suite.offline.cli;

import static com.nokia.netguard.adapter.test.suite.offline.requests.Constants.PRINT_EXIT_CODE_SUFFIX;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.LinkedList;
import java.util.List;

import com.nokia.netguard.adapter.cli.InteractiveCommand;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nakina.adapter.api.requestarguments.sessionmanagement.AuthenticationArguments;
import com.nakina.adapter.api.type.AdapterInterface;
import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.CLICommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.callback.Callback;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Executor.class})
public class ExecutorTest {

    static final String DEFAULT_MASK = "********";
    static final Integer DEFAULT_TIMEOUT = 60000;
    static final String DEFAULT_PROMPT = "\\$$|\\$ $|#$|# $";
    static final String SUDO_PROMPT = "\\[sudo\\] password for .+?:";
    static final String SUDO_PASSWORD = "password";

    static final Integer EXIT_CODE_NOT_CHECKED = -1;
    static final Integer SUCCESSFUL_EXECUTION_EXIT_CODE = 0;

    static final String PROMPT = "[test@hostname]$";
    static final String COMMAND = "command";
    static final String CLEAN_COMMAND_OUTPUT = "command executed";
    static final String COMMAND_OUTPUT = COMMAND + "\n" + CLEAN_COMMAND_OUTPUT + "\n" + PROMPT;
    static final String COMMAND_OUTPUT_WITH_EXIT_CODE = COMMAND + "\n" + CLEAN_COMMAND_OUTPUT + "\n" + SUCCESSFUL_EXECUTION_EXIT_CODE + "\n" + PROMPT;

    static final byte[] CONFIG_FILE = "<adapterConfiguration><ne><lineTermination>\\n</lineTermination></ne></adapterConfiguration>".getBytes();

    Executor executor;

    LinkedList<CLICommand> commandList;
    CliCommandFactory cliCommandFactory = new CliCommandFactory();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        BaseCommand base = mock(BaseCommand.class);

        AuthenticationArguments authenticationArguments = mock(AuthenticationArguments.class);
        doReturn(authenticationArguments).when(base).getAuthenticationArguments(any());

        AdapterInterface adapterInterface = mock(AdapterInterface.class);
        doReturn(CONFIG_FILE).when(adapterInterface).getResource(any());
        doReturn(adapterInterface).when(base).getAdapterInterface();

        whenNew(CLICommand.class)
                .withArguments(any(), any(String.class), any(String.class), any(String.class), any(String.class), any(Integer.class))
                .thenAnswer(cliCommandFactory);
        
        commandList = new LinkedList<>();
        executor = new Executor(base);
    }

    @After
    public void destroy() {
        assertTrue("Creation of more CLICommands was expected (" + commandList.size() +
                " more to be created).", commandList.isEmpty());
    }

    @Test
    public void executeCommandTest() throws Exception {
        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n").when(cliCommand).removeEchoAndPrompt();

        Callback callback = mock(Callback.class);

        Command command = Command.create(COMMAND).addCallback(callback);
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + "\n"), any(String.class), eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
        verify(callback, times(1)).call(eq(new ComparableExecutionResult(COMMAND_OUTPUT, CLEAN_COMMAND_OUTPUT, EXIT_CODE_NOT_CHECKED)));
    }

    @Test
    public void executeInteractiveCommandTest() throws Exception {
        CLICommand cliCommand1 = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n").when(cliCommand1).removeEchoAndPrompt();

        CLICommand cliCommand2 = mockCliCommand(COMMAND_OUTPUT_WITH_EXIT_CODE);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n" + SUCCESSFUL_EXECUTION_EXIT_CODE + "\n").when(cliCommand2).removeEchoAndPrompt();

        Callback callback = mock(Callback.class);
        InteractiveCommand interactiveCommand = new InteractiveCommand();
        interactiveCommand.retrieveExitCode().addCallback(callback);
        Callback singleCommandCallback = mock(Callback.class);
        interactiveCommand.add(Command.create(COMMAND).addCallback(singleCommandCallback));
        interactiveCommand.add(Command.create(COMMAND));
        ExecutionResult executionResult = executor.execute(interactiveCommand);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + PRINT_EXIT_CODE_SUFFIX + "\n"), any(String.class),
                eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand1, times(1)).send();

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + "\n"), any(String.class),
                eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand2, times(1)).send();

        InOrder inOrder = inOrder(singleCommandCallback, callback);
        inOrder.verify(singleCommandCallback, times(1)).call(eq(new ComparableExecutionResult(COMMAND_OUTPUT, CLEAN_COMMAND_OUTPUT, EXIT_CODE_NOT_CHECKED)));
        inOrder.verify(callback, times(1)).call(eq(new ComparableExecutionResult(COMMAND_OUTPUT, CLEAN_COMMAND_OUTPUT, EXIT_CODE_NOT_CHECKED)));
        inOrder.verify(callback, times(1)).call(eq(new ComparableExecutionResult(COMMAND_OUTPUT_WITH_EXIT_CODE, CLEAN_COMMAND_OUTPUT,
                SUCCESSFUL_EXECUTION_EXIT_CODE)));
        assertEquals(new ComparableExecutionResult(COMMAND_OUTPUT + "\n" + COMMAND_OUTPUT_WITH_EXIT_CODE,
                CLEAN_COMMAND_OUTPUT + "\n" + CLEAN_COMMAND_OUTPUT, SUCCESSFUL_EXECUTION_EXIT_CODE), executionResult);
    }

    @Test
    public void callMultipleCallbacksInProperOrderTest() throws RequestFailure {
        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n").when(cliCommand).removeEchoAndPrompt();

        Callback callback1 = mock(Callback.class);
        Callback callback2 = mock(Callback.class);
        InOrder inOrder = inOrder(callback1, callback2);

        Command command = Command.create(COMMAND).addCallback(callback1).addCallback(callback2);
        executor.execute(command);

        verify(cliCommand, times(1)).send();
        ComparableExecutionResult executionResult = new ComparableExecutionResult(COMMAND_OUTPUT, CLEAN_COMMAND_OUTPUT, EXIT_CODE_NOT_CHECKED);
        inOrder.verify(callback1, times(1)).call(executionResult);
        inOrder.verify(callback2, times(1)).call(executionResult);
    }

    @Test
    public void executeMaskedCommandTest() throws Exception {
        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n").when(cliCommand).removeEchoAndPrompt();

        Command command = Command.create(COMMAND).setMasked();
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + "\n"), eq(DEFAULT_MASK), eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
    }

    @Test
    public void executeSudoCommandTest() throws Exception {
        final String SUDO_PREFIX = "sudo ";
        final String SUDO_PASSWORD_REQUEST = "[sudo] password for user:";

        CLICommand cliCommand = mockCliCommand(COMMAND + "\n" + SUDO_PASSWORD_REQUEST);
        doReturn(EMPTY).when(cliCommand).removeEchoAndPrompt();

        CLICommand sudoPasswordCliCommand = mockCliCommand(CLEAN_COMMAND_OUTPUT + "\n" + PROMPT);
        doReturn(EMPTY).when(sudoPasswordCliCommand).removeEchoAndPrompt();
        Callback callback = mock(Callback.class);

        Command command = Command.create(COMMAND).escalateCommand(SUDO_PASSWORD).addCallback(callback);
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(SUDO_PREFIX + COMMAND + "\n"), any(String.class),
                eq(DEFAULT_PROMPT + "|" + SUDO_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(SUDO_PASSWORD + "\n"), eq(DEFAULT_MASK),
                eq(DEFAULT_PROMPT + "|" + SUDO_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(sudoPasswordCliCommand, times(1)).send();

        verify(callback, times(1)).call(eq(new ComparableExecutionResult(
                COMMAND + "\n" + SUDO_PASSWORD_REQUEST + CLEAN_COMMAND_OUTPUT + "\n" + PROMPT, EMPTY, EXIT_CODE_NOT_CHECKED)));
    }

    @Test
    public void executeNewLineEndedCommandTest() throws Exception {
        final String COMMAND_ENDED_WITH_NEWLINE = COMMAND + "\n";
        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT).when(cliCommand).removeEchoAndPrompt();

        Command command = Command.create(COMMAND_ENDED_WITH_NEWLINE);
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + "\n"), any(String.class), eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
    }

    @Test
    public void useCustomCommandNameTest() throws Exception {
        final String COMMAND_NAME = "commandName";

        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT);
        doReturn(CLEAN_COMMAND_OUTPUT).when(cliCommand).removeEchoAndPrompt();

        Command command = Command.create(COMMAND).setName(COMMAND_NAME);
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND_NAME), eq(COMMAND + "\n"), any(String.class), eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
    }

    @Test
    public void retrieveExitCodeOfExecutedCommandTest() throws Exception {
        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT_WITH_EXIT_CODE);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n" + SUCCESSFUL_EXECUTION_EXIT_CODE + "\n").when(cliCommand).removeEchoAndPrompt();
        Callback callback = mock(Callback.class);

        Command command = Command.create(COMMAND).retrieveExitCode().addCallback(callback);
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + PRINT_EXIT_CODE_SUFFIX + "\n"), any(String.class),
                eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
        verify(callback, times(1)).call(eq(new ComparableExecutionResult(COMMAND_OUTPUT_WITH_EXIT_CODE, CLEAN_COMMAND_OUTPUT, SUCCESSFUL_EXECUTION_EXIT_CODE)));
    }

    @Test
    public void invalidExitCodeStringTest() throws Exception {
        final String INVALID_EXIT_CODE = "INVALID";
        final String COMMAND_OUTPUT_WITH_INVALID_EXIT_CODE = COMMAND + "\n" + CLEAN_COMMAND_OUTPUT + "\n" + INVALID_EXIT_CODE + "\n" + PROMPT;

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Cannot parse exit code of executed command to a number"));

        CLICommand cliCommand = mockCliCommand(COMMAND_OUTPUT_WITH_INVALID_EXIT_CODE);
        doReturn(CLEAN_COMMAND_OUTPUT + "\n" + INVALID_EXIT_CODE + "\n").when(cliCommand).removeEchoAndPrompt();

        Command command = Command.create(COMMAND).retrieveExitCode();
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + PRINT_EXIT_CODE_SUFFIX + "\n"), any(String.class),
                eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
    }

    @Test
    public void incompleteOutputForExitCodeRetrievalTest() throws Exception {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Output is incomplete, contains only single line"));

        CLICommand cliCommand = mockCliCommand("INVALID OUTPUT");
        doReturn(EMPTY).when(cliCommand).removeEchoAndPrompt();

        Command command = Command.create(COMMAND).retrieveExitCode();
        executor.execute(command);

        verifyNew(CLICommand.class).withArguments(any(), eq(COMMAND), eq(COMMAND + PRINT_EXIT_CODE_SUFFIX + "\n"), any(String.class),
                eq(DEFAULT_PROMPT), eq(DEFAULT_TIMEOUT));
        verify(cliCommand, times(1)).send();
    }

    @Test
    public void executeNullCommandTest() throws RequestFailure {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Command object is null"));

        Command command = null;
        executor.execute(command);
    }

    @Test
    public void executeNullInteractiveCommandTest() throws RequestFailure {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Unable to execute empty interactive command"));

        InteractiveCommand interactiveCommand = null;
        executor.execute(interactiveCommand);
    }

    @Test
    public void executeEmptyInteractiveCommandTest() throws RequestFailure {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Unable to execute empty interactive command"));

        InteractiveCommand interactiveCommand = new InteractiveCommand();
        executor.execute(interactiveCommand);
    }

    @Test
    public void executeCommandWithNullStringTest() throws RequestFailure {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(StringContains.containsString("The command string cannot be null"));

        Command command = Command.create(null);
        executor.execute(command);
    }

    @Test
    public void executeSudoCommandWithNullPasswordTest() throws RequestFailure {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Password for sudo command is not set"));

        Command command = Command.create(COMMAND).escalateCommand();
        executor.execute(command);
    }

    CLICommand mockCliCommand(String output) throws RequestFailure {
        CLICommand cliCommand = mock(CLICommand.class);
        doReturn(output).when(cliCommand).send();
        commandList.add(cliCommand);
        return cliCommand;
    }

    class CliCommandFactory implements Answer<CLICommand> {
        @Override
        public CLICommand answer(InvocationOnMock invocation) throws Throwable {
            if (commandList.isEmpty()) {
                throw new IllegalStateException("Creation of CLICommand not expected.");
            }
            return commandList.removeFirst();
        }
    }

    class ComparableExecutionResult extends ExecutionResult {

        public ComparableExecutionResult(String output, String cleanOutput, Integer exitCode) {
            super(output, cleanOutput);
            setExitCode(exitCode);
        }

        @Override
        public boolean equals(Object object) {
            ExecutionResult executionResult = (ExecutionResult)object;
            return this.getOutput().equals(executionResult.getOutput())
                    && this.getCleanOutput().equals(executionResult.getCleanOutput())
                    && this.getExitCode() == executionResult.getExitCode();
        }
    }
}
