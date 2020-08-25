package com.nokia.netguard.adapter.test.suite.offline.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.callback.Callback;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.MatchOutput;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

public class CommandTest {

    @Test
    public void promptConcatenationTest() {
        final String[] PROMPT_PARTS = new String[]{"prompt1", "prompt2", "prompt3"};
        final String PROMPT_DELIMITER = "|";

        Command command = new Command();
        assertEquals(null, command.getPrompt());

        Stream.of(PROMPT_PARTS).forEach(prompt -> command.addPrompt(prompt));
        assertEquals(String.join(PROMPT_DELIMITER, PROMPT_PARTS), command.getPrompt());
    }

    @Test
    public void createEmptyCommandTest() {
        Command emptyCommand = Command.create();
        assertEquals("", emptyCommand.getCommand());
    }

    @Test
    public void createCommandTest() {
        final String COMMAND = "command";

        Command command = Command.create(COMMAND);
        assertEquals(COMMAND, command.getCommand());
    }

    @Test
    public void createCommandWithArgumentsTest() {
        final String COMMAND = "command";
        final String[] ARGUMENTS = {"firstArgument", "secondArgument", "thirdArgument"};

        Command command = Command.create(COMMAND, ARGUMENTS[0], ARGUMENTS[1], ARGUMENTS[2]);
        assertEquals(COMMAND + " " + ARGUMENTS[0]  + " " + ARGUMENTS[1] + " " + ARGUMENTS[2], command.getCommand());
    }

    @Test
    public void setNullOrEmptyPromptTest() {
        Command command = new Command();
        assertEquals(null, command.getPrompt());

        command.addPrompt(null);
        assertEquals(null, command.getPrompt());

        command.addPrompt("");
        assertEquals(null, command.getPrompt());
    }

    @Test
    public void escalateCommandWithPasswordTest() {
        final String SUDO_PASSWORD = "password";

        Command command = new Command();
        command.escalateCommand(SUDO_PASSWORD);

        assertTrue(command.isEscalated());
        assertEquals(SUDO_PASSWORD, command.getEscalatePassword());
    }

    @Test
    public void addMultipleCallbacksTest() {
        List<Callback> callbacks = new LinkedList<>();
        callbacks.add(VerifyOutput.create());
        callbacks.add(MatchOutput.create("parameter"));
        callbacks.add(CheckExitCode.create());

        Command command = new Command();
        callbacks.forEach(command::addCallback);

        assertEquals(callbacks.size(), command.getCallbacks().size());
        assertEquals(callbacks, command.getCallbacks());
    }
}
