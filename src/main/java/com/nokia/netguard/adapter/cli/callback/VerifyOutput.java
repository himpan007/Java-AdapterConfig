package com.nokia.netguard.adapter.cli.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;

public class VerifyOutput implements Callback {

	private List<String> expectedOutput = new ArrayList<>();
	private List<String> erroneousOutput = new ArrayList<>();

	public static VerifyOutput create() {
		return new VerifyOutput();
	}

	public VerifyOutput addExpectedOutput(String expected) {
		expectedOutput.add(expected);
		return this;
	}

	public VerifyOutput addExpectedOutput(String[] expected) {
		expectedOutput.addAll(Arrays.asList(expected));
		return this;
	}

	public VerifyOutput addErroneousOutput(String[] erroneous) {
		erroneousOutput.addAll(Arrays.asList(erroneous));
		return this;
	}

	public VerifyOutput addErroneousOutput(String erroneous) {
		erroneousOutput.add(erroneous);
		return this;
	}

	@Override
	public void call(ExecutionResult executionResult) throws RequestFailure {
		String output = executionResult.getOutput();
		if (!erroneousOutput.isEmpty() && containsErroneousOutput(output)) {
			throw new RequestFailure("Output contains errors: " + output);
		}
		if (!expectedOutput.isEmpty() && !containsExpectedOutput(output)) {
			throw new RequestFailure("Unexpected output. One of " + expectedOutput.toString() + " was expected but got: " + output);
		}
	}

	private boolean containsExpectedOutput(String output) {
		return expectedOutput.stream().parallel().anyMatch(output::contains);
	}

	private boolean containsErroneousOutput(String output) {
		return erroneousOutput.stream().parallel().anyMatch(output::contains);
	}
}
