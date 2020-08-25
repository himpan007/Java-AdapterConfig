package com.nokia.netguard.adapter.test.suite.offline.cli;

import com.nokia.netguard.adapter.cli.InteractiveCommand;
import com.nokia.netguard.adapter.cli.callback.Callback;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.MatchOutput;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InteractiveCommandTest {

    @Test
    public void addMultipleCallbacksTest() {
        List<Callback> callbacks = new LinkedList<>();
        callbacks.add(VerifyOutput.create());
        callbacks.add(MatchOutput.create("parameter"));
        callbacks.add(CheckExitCode.create());

        InteractiveCommand interactiveCommand = new InteractiveCommand();
        callbacks.forEach(interactiveCommand::addCallback);

        assertEquals(callbacks.size(), interactiveCommand.getCallbacks().size());
        assertEquals(callbacks, interactiveCommand.getCallbacks());
    }
}
