package com.nokia.netguard.adapter.requests.crud;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.nesecurity.CreateUserRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

/**
 * Create User on the NE.
 *
 */
public class NESecurityCreateUserRequestHandler extends CreateUserRequestHandlerBase {

	public void createUser(String accountName, String userName, String password) throws RequestFailure {
		Executor executor = new Executor(this);
		executor.execute(Command
				.create("useradd " + userName)
				.escalateCommand()
				.retrieveExitCode()
				.addCallback(CheckExitCode.create())
				.addCallback(VerifyOutput
						.create()
						.addErroneousOutput("user '" + userName + "' already exists")));
		try {
			NESecurityEditUserRequestHandler.changeUserPassword(this, userName, password);
		} catch (Exception e) {
			try {
				// delete user when error occur during password change
				NESecurityDeleteUserRequestHandler.deleteUser(this, userName);
			} catch (Exception ex) {
				traceError(getClass(), "Error during cleaning after user creation. User may not exist on the system. Error message: "
						+ ex.getMessage());
				throw ex;
			}
			throw e;
		}
	}
}
