package com.nokia.netguard.adapter.test.suite.offline.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;

public class ExecutionResultTest {

    final Integer SUCCESSFUL_EXECUTION_EXIT_CODE = 0;
    final Integer UNSUCCESSFUL_EXECUTION_EXIT_CODE = 1;

    static final String OUTPUT = "command\noutput\nprompt";
    static final String CLEAN_OUTPUT = "output\n";

    ExecutionResult executionResult = new ExecutionResult(OUTPUT, CLEAN_OUTPUT);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void checkSuccessfulExecutionTest() throws RequestFailure {
        executionResult.setExitCode(SUCCESSFUL_EXECUTION_EXIT_CODE);
        assertTrue(executionResult.isSuccessful());
    }

    @Test
    public void checkUnsuccessfulExecutionTest() throws RequestFailure {
        executionResult.setExitCode(UNSUCCESSFUL_EXECUTION_EXIT_CODE);
        assertFalse(executionResult.isSuccessful());
    }

    @Test
    public void exitCodeNotCheckedTest() throws RequestFailure {
        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Cannot determine whether execution was successful or not"));

        executionResult.isSuccessful();
    }


}
