package com.nokia.netguard.adapter.test.suite.offline.cli.callback;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.callback.MatchOutput;

public class MatchOutputTest {

    static final String PARAMETER_NAME = "parameter";
    static final Integer EXIT_CODE = 0;

    MatchOutput matchOutput = new MatchOutput(PARAMETER_NAME);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateInstanceTest() {
        MatchOutput matchOutput = MatchOutput.create(PARAMETER_NAME);
        assertTrue(matchOutput != null);
        assertTrue(matchOutput instanceof MatchOutput);
    }

    @Test(expected = Test.None.class)
    public void verifyIfOutputMatchesPatternTest() throws RequestFailure {
        final String OUTPUT = "everything works fine, congratulations!";
        final String PATTERN = "^everything.*fine.*";

        ExecutionResult executionResult = new ExecutionResult(OUTPUT, OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        matchOutput.setPattern(PATTERN);
        matchOutput.call(executionResult);
    }

    @Test
    public void throwWhenOutputDoesntMatchPatternTest() throws RequestFailure {
        final String OUTPUT = "some errors occurred";
        final String PATTERN = ".*fine.*";

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Output does not match provided pattern"));

        ExecutionResult executionResult = new ExecutionResult(OUTPUT, OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        matchOutput.setPattern(PATTERN);
        matchOutput.call(executionResult);
    }

    @Test
    public void defaultPatternFlagsTest() throws RequestFailure {
        // default flags are: CASE_INSENSITIVE | DOTALL
        final String OUTPUT = "EVERYTHING\nFINE";
        final String PATTERN = "everything.*fine";

        ExecutionResult executionResult = new ExecutionResult(OUTPUT, OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        matchOutput.setPattern(PATTERN);
        matchOutput.call(executionResult);
    }

    @Test
    public void customPatternFlagsTest() throws RequestFailure {
        final String OUTPUT = "EVERYTHING\nFINE";
        final String PATTERN = "everything.*fine";

        expectedException.expect(RequestFailure.class);
        expectedException.expectMessage(StringContains.containsString("Output does not match provided pattern"));

        ExecutionResult executionResult = new ExecutionResult(OUTPUT, OUTPUT);
        executionResult.setExitCode(EXIT_CODE);

        matchOutput.setPattern(PATTERN).setFlags(CASE_INSENSITIVE);
        matchOutput.call(executionResult);
    }
}
