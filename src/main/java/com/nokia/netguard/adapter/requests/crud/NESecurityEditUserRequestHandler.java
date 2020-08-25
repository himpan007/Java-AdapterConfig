package com.nokia.netguard.adapter.requests.crud;

import java.util.LinkedList;
import java.util.List;

import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.nesecurity.EditUserRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.InteractiveCommand;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

/**
 * Edit existing user credentials on the NE.
 */
public class NESecurityEditUserRequestHandler extends EditUserRequestHandlerBase {
	private static final String[] ERROR_MESSAGES = {"Usage:", "Unknown user name", "passwords do not match", "does not exist"};

	@Override
	public void editUser(String accountName, String userName) throws RequestFailure {
		changeUserPassword(this, userName, getPassword());
	}

	public static void changeUserPassword(BaseCommand base, String userName, String password) throws RequestFailure {
		VerifyOutput errorChecks = VerifyOutput.create().addErroneousOutput(ERROR_MESSAGES);

		InteractiveCommand interactiveCommand = new InteractiveCommand();
		interactiveCommand.retrieveExitCode().addCallback(errorChecks);
		interactiveCommand.add(Command.create("passwd " + userName).escalateCommand().addPrompt("[N|n]ew.+password:( ?)$"));
		interactiveCommand.add(Command.create(password).setMasked().addPrompt("([R|r]etype )?[N|n]ew.+password:( ?)$"));
		interactiveCommand.add(Command.create(password).setMasked()
				.addCallback(CheckExitCode.create())
				.addCallback(VerifyOutput.create()
						.addExpectedOutput("all authentication tokens updated successfully")
						.addExpectedOutput("passwd: password updated successfully")));
		Executor executor = new Executor(base);
		executor.execute(interactiveCommand);
	}
}