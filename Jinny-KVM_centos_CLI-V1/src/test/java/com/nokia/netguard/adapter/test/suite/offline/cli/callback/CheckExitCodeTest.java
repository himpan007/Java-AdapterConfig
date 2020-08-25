package com.nokia.netguard.adapter.test.suite.offline.cli.callback;

import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;

public class CheckExitCodeTest {

    static final String OUTPUT = "output";

    CheckExitCode checkExitCode = new CheckExitCode();

    ExecutionResult executionResult = new ExecutionResult(OUTPUT, OUTPUT);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateInstanceTest() {
        CheckExitCode checkExitCode = CheckExitCode.create();
        assertTrue(checkExitCode != null);
        assertTrue(checkExitCode instanceof CheckExitCode);
    }

    @Test(expected = Test.None.class)
    public void successfulExitCodeTest() throws RequestFailure {
        final Integer SUCCESSFUL_EXECUTION_EXIT_CODE = 0;
        executionResult.setExitCode(SUCCESSFUL_EXECUTION_EXIT_CODE);
        checkExitCode.call(executionResult);
    }

    @Test
    public void unsuccessfulExitCodeTest() throws RequestFailure {
        final Integer UNSUCCESSFUL_EXECUTION_EXIT_CODE = 1;

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Command execution failed"));

        executionResult.setExitCode(UNSUCCESSFUL_EXECUTION_EXIT_CODE);
        checkExitCode.call(executionResult);
    }
}
