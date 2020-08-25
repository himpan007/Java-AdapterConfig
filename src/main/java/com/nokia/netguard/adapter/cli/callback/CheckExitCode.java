package com.nokia.netguard.adapter.cli.callback;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;

public class CheckExitCode implements Callback {

	private static final Integer SUCCESSFUL_EXECUTION_EXIT_CODE = 0;

	public static CheckExitCode create() {
		return new CheckExitCode();
	}

	@Override
	public void call(ExecutionResult executionResult) throws RequestFailure {
		Integer exitCode = executionResult.getExitCode();
		if (exitCode != SUCCESSFUL_EXECUTION_EXIT_CODE) {
			throw new RequestFailure("Command execution failed. Exit code: " + exitCode + "Command output: " + executionResult.getCleanOutput());
		}
	}
}
