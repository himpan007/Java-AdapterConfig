package com.nokia.netguard.adapter.test.ngagent;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.test.ngagent.command.Command;
import com.nokia.netguard.adapter.test.ngagent.session.SessionContext;
import com.nokia.netguard.adapter.test.ngagent.session.SessionManagerMock;
import com.nokia.netguard.adapter.test.suite.TestSuite;

public class CLICommandMock {

	Command cmd = null;
	String readUntil = "";
	private String result;
	private String command;
	private CommandAndResponseList commandAndResponse;

	public CLICommandMock(CommandAndResponseList commandAndResponse, String command, String readUntilString,
			String address) {
		this.command = command;
		this.commandAndResponse = commandAndResponse;

		if (!address.contains(TestSuite.OFFLINE_TEST)) {
			SessionManagerMock manager = SessionContext.getManager();
			try {
				cmd = new Command(manager.getChannel(), command, command, readUntilString, 30000);
			} catch (IOException e) {
				RuntimeException ex = new RuntimeException(e.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		}
	}

	public String send() throws RequestFailure {
		if (cmd != null) {
			result = cmd.send();
		} else {
			result = commandAndResponse.getResponse(command);
		}

		Trace.info(getClass(), "command: " + command + " response: " + result  );

		return result;
	}

	public String removeEchoAndPrompt() {
		if (result == null) {
			return null;
		} else if (result.split("\n").length <= 2) {
			return result;
		}

		String[] splitedResult = result.split("\n");
		String[] subArray = Arrays.copyOfRange(splitedResult, 1, splitedResult.length - 1);
		
		return Arrays.stream(subArray).collect(Collectors.joining("\n"));
	}

	public void addPattern(String pattern) {
		cmd.addPattern(pattern);
	}

	public String getMatchingPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	public String flush() {
		return null;
	}

}
