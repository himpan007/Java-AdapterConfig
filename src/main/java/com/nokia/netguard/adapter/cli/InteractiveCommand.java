package com.nokia.netguard.adapter.cli;

import com.nokia.netguard.adapter.cli.callback.Callback;

import java.util.LinkedList;
import java.util.List;

public class InteractiveCommand extends LinkedList<Command> {

	private List<Callback> callbacks = new LinkedList<>();

	private boolean retrieveExitCode = false;

	/**
	 * Returns list of callbacks to be called after execution of every subcommand, which is a part of whole interactive command.
	 * @return List of callbacks.
	 */
	public List<Callback> getCallbacks() {
		return callbacks;
	}

	/**
	 * Adds a callback to be called after execution of every subcommand, which is a part of whole interactive command. Multiple callbacks may be added, they will be called in the same order as they have been added.
	 * @param callback Callback to be called when every subcommand execution is ready.
	 * @return Current Command instance.
	 */
	public InteractiveCommand addCallback(Callback callback) {
		callbacks.add(callback);
		return this;
	}

	/**
	 * By calling this command we qualify interactive command to have exit code retrieved after execution.
	 */
	public InteractiveCommand retrieveExitCode() {
		this.retrieveExitCode = true;
		return this;
	}

	/**
	 * Returns information whether interactive command should have exit code retrieved after execution.
	 * @return True if command will have exit code retrieved, false otherwise.
	 */
	public boolean shouldRetrieveExitCode() {
		return this.retrieveExitCode;
	}
}
