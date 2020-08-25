package com.nokia.netguard.adapter.test.suite.realne.requests.crud;

import java.util.Collection;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.crud.NESecurityCreateUserRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.RealNESuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	LogoutRequestHandler.class,
	LoginRequestHandler.class,
	Executor.class,
	NESecurityCreateUserRequestHandler.class })
public class NESecurityCreateUserTest extends AdapterJUnitBase {

	private NESecurityCreateUserRequestHandler createUserRequestHandler;
	private Executor exec;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public NESecurityCreateUserTest(NE ne) {
		super(ne);
	}

	@Before
	public void setUpConnection() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());

		createUserRequestHandler = (NESecurityCreateUserRequestHandler) mockRequestHandler(
				new NESecurityCreateUserRequestHandler());
		
		exec = new Executor(createUserRequestHandler);
		exec.execute(Command.create("useradd existingUser").escalateCommand());
	}

	@After
	public void teardown() throws Exception {
		exec.execute(Command.create("userdel newUser").escalateCommand());
		exec.execute(Command.create("userdel existingUser").escalateCommand());

		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void createUserTest() throws Exception {
		String username = "newUser";
		String password = "newUserPass";
		createUserRequestHandler.createUser("User Accounts", username, password);
	}

	@Test
	public void createExistingUserTest() throws Exception {
		String username = "existingUser";
		String password = "existingUserPass";
		String userAlreadyExistsMessage = "Command execution failed";

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(StringContains.containsString(userAlreadyExistsMessage));

		createUserRequestHandler.createUser("User Accounts", username, password);
	}
}
