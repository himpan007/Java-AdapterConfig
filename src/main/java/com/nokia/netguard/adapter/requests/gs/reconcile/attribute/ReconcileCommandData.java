package com.nokia.netguard.adapter.requests.gs.reconcile.attribute;

public class ReconcileCommandData {
	private String command;
	private Integer timeout;

	public ReconcileCommandData(String command, Integer timeout) {
		super();
		this.command = command;
		this.timeout = timeout;
	}

	public String getCommand() {
		return command;
	}

	public Integer getTimeout() {
		return timeout;
	}
}
