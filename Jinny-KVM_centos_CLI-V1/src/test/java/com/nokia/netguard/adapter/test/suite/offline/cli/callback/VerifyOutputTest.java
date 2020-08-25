package com.nokia.netguard.adapter.test.suite.offline.cli.callback;

import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

public class VerifyOutputTest {

    static final Integer EXIT_CODE = 0;

    VerifyOutput verifyOutput = new VerifyOutput();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateInstanceTest() {
        VerifyOutput verifyOutput = VerifyOutput.create();
        assertTrue(verifyOutput != null);
        assertTrue(verifyOutput instanceof VerifyOutput);
    }

    @Test(expected = Test.None.class)
    public void verifyIfOutputContainsExpectedOutputTest() throws RequestFailure {
        final String EXPECTED_OUTPUT = "expected output";

        ExecutionResult executionResult = new ExecutionResult(EXPECTED_OUTPUT, EXPECTED_OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        verifyOutput.addExpectedOutput(EXPECTED_OUTPUT);
        verifyOutput.call(executionResult);
    }

    @Test
    public void throwWhenOutputDoesntContainExpectedOutputTest() throws RequestFailure {
        final String DIFFERENT_OUTPUT = "different output";

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Unexpected output"));

        ExecutionResult executionResult = new ExecutionResult(DIFFERENT_OUTPUT, DIFFERENT_OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        verifyOutput.addExpectedOutput(new String[]{"expected output", "another also expected output"});
        verifyOutput.call(executionResult);
    }

    @Test
    public void throwWhenOutputContainsErrorTest() throws RequestFailure {
        final String ERRONEOUS_OUTPUT = "error: it doesn't work";

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Output contains errors"));

        ExecutionResult executionResult = new ExecutionResult(ERRONEOUS_OUTPUT, ERRONEOUS_OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        verifyOutput.addErroneousOutput(ERRONEOUS_OUTPUT);
        verifyOutput.call(executionResult);
    }

    @Test(expected = Test.None.class)
    public void verifyIfOutputDoesntContainErrorsTest() throws RequestFailure {
        final String VALID_OUTPUT = "valid output";
        final String ERRONEOUS_OUTPUT = "error: it doesn't work";

        ExecutionResult executionResult = new ExecutionResult(VALID_OUTPUT, VALID_OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        verifyOutput.addErroneousOutput(ERRONEOUS_OUTPUT);
        verifyOutput.call(executionResult);
    }
}
