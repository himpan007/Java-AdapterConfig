package com.nokia.netguard.adapter.requests.crud;

import java.util.LinkedList;
import java.util.List;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.nesecurity.EditMyPasswordRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.InteractiveCommand;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

/**
 * Create User on the NE.
 */
public class NESecurityEditMyPasswordRequestHandler extends EditMyPasswordRequestHandlerBase {
	private static final String[] ERROR_MESSAGES = {
			"Usage:",
			"Authentication token manipulation error",
			"BAD PASSWORD",
			"passwords do not match",
			"Unknown user name",
			"does not exist"
	};

	@Override
	public void editMyPassword(String accountName, String userName, String newPassword, String oldPassword) throws RequestFailure {
		VerifyOutput errorCallback = VerifyOutput.create().addErroneousOutput(ERROR_MESSAGES);

		InteractiveCommand interactiveCommand = new InteractiveCommand();
		interactiveCommand.retrieveExitCode().addCallback(errorCallback);
		interactiveCommand.add(Command.create("passwd").addPrompt("\\(current\\) UNIX password:( ?)$"));
		interactiveCommand.add(Command.create(oldPassword).setMasked().addPrompt("[N|n]ew.+password:( ?)$"));
		interactiveCommand.add(Command.create(newPassword).setMasked().addPrompt("([R|r]etype )?[N|n]ew.+password:( ?)$"));
		interactiveCommand.add(Command.create(newPassword).setMasked()
				.addCallback(CheckExitCode.create())
				.addCallback(VerifyOutput.create()
						.addExpectedOutput("all authentication tokens updated successfully")
						.addExpectedOutput("passwd: password updated successfully"))
		);
		Executor executor = new Executor(this);
		executor.execute(interactiveCommand);
	}
}
