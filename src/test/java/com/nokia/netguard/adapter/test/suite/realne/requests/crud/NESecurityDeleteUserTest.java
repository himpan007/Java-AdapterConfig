package com.nokia.netguard.adapter.test.suite.realne.requests.crud;

import java.util.Collection;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
import com.nokia.netguard.adapter.requests.crud.NESecurityDeleteUserRequestHandler;
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
	NESecurityDeleteUserRequestHandler.class })
public class NESecurityDeleteUserTest extends AdapterJUnitBase {

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public NESecurityDeleteUserTest(NE ne) {
		super(ne);
	}

	//private NE ne;
	private Executor exec;
	private NESecurityDeleteUserRequestHandler deleteUserRequestHandler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpConnection() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());

		deleteUserRequestHandler = (NESecurityDeleteUserRequestHandler) mockRequestHandler(new NESecurityDeleteUserRequestHandler());
		
		exec = new Executor(deleteUserRequestHandler);
		exec.execute(Command.create("useradd existingUser").escalateCommand());
	}

	@After
	public void teardown() throws Exception {
		exec.execute(Command.create("userdel existingUser").escalateCommand());
		
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void deleteUserTest() throws Exception {
		String username = "existingUser";
		deleteUserRequestHandler.deleteUser("User Accounts", username);
	}

	@Test
	public void deleteNotExistingUserTest() throws Exception {
		String username = "nonexistentUser";
		String userDoesNotExistMessage = "userdel: user '" + username + "' does not exist|Command execution failed";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(matcher(userDoesNotExistMessage));

		deleteUserRequestHandler.deleteUser("User Accounts", username);
	}
	
}
