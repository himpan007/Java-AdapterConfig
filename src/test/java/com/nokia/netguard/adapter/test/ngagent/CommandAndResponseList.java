package com.nokia.netguard.adapter.test.ngagent;

import java.util.ArrayList;
import java.util.List;

import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.RequestFailure;

public class CommandAndResponseList {

	private List<Entry> entries = new ArrayList<>();
	private boolean ignoreCommandOrder = false;

	public void setIgnoreCommandOrder(boolean ignoreCommandOrder) {
		this.ignoreCommandOrder = ignoreCommandOrder;
	}

	public void addCommandAndResponse(String command, String response) {
		entries.add(new Entry(command, command + "\n" + response));
	}

	public String getResponse(String command) throws RequestFailure {
		Trace.info(getClass(), "Incoming command: " + command);
		if (entries.size() == 0) {
			throw new RequestFailure("Unexpected command: " + command);
		}
		String commandTrimmed = command.replaceAll("\n", "");
		Entry entry = getCommand(commandTrimmed);
		if (!entry.getCommand().equals(commandTrimmed)) {
			throw new RequestFailure(
					"Another command expected. Expected: " + entry.getCommand() + ", received: " + commandTrimmed);
		}
		String response = entry.getResponse();
		Trace.info(getClass(), "Response: " + response + " on command: " + command);
		return response;
	}

	private Entry getCommand(String command) throws RequestFailure {
		if (ignoreCommandOrder) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).command.equals(command)) {
					return entries.remove(i);
				}
			}
			throw new RequestFailure("Command: " + command + " not found. Command order ignore mode is on.");
		}

		return entries.remove(0);
	}

	public boolean allCommandsExecuted() {
		return entries.isEmpty();
	}

	class Entry {

		private String command;
		private String response;

		Entry(String command, String response) {
			this.command = command;
			this.response = response;
		}

		String getCommand() {
			return command;
		}

		String getResponse() {
			return response;
		}

		@Override
		public String toString() {
			return "Entry [command=" + command + ", response=" + response + "]";
		}

	}
}
