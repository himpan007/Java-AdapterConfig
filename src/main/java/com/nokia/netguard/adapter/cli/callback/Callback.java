package com.nokia.netguard.adapter.cli.callback;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;

public interface Callback {
	/**
	 * Callback method will be called after execution of command
	 * 
	 * @param executionResult Result od command execution
	 * @throws RequestFailure
	 */
	public void call(ExecutionResult executionResult) throws RequestFailure;
}
