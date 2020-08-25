package com.nokia.netguard.adapter.requests.crud;

import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.nesecurity.DeleteUserRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

/**
 * Delete user credential from NE.
 *
 */
public class NESecurityDeleteUserRequestHandler extends DeleteUserRequestHandlerBase {

	private static final String[] ERROR_MESSAGES = {
			"Usage:",
			"does not exist"
		};

	@Override
	public void deleteUser(String accountName, String userName) throws RequestFailure {
		deleteUser(this, userName);
	}

	/**
	 * This method is used in create user request when exception is thrown during execution
	 * @param base
	 * @param userName
	 * @throws RequestFailure
	 */
	public static void deleteUser(BaseCommand base, String userName) throws RequestFailure {
		Executor executor = new Executor(base);
		executor.execute(Command
				.create("userdel " + userName)
				.escalateCommand()
				.retrieveExitCode()
				.addCallback(CheckExitCode.create())
				.addCallback(
						VerifyOutput
						.create()
						.addErroneousOutput(ERROR_MESSAGES)
						)
				);
	}

}
