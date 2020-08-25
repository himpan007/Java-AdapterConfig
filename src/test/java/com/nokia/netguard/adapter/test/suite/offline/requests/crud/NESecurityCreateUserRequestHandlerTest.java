package com.nokia.netguard.adapter.test.suite.offline.requests.crud;

import java.util.Collection;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.crud.NESecurityCreateUserRequestHandler;
import com.nokia.netguard.adapter.requests.crud.NESecurityDeleteUserRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	LogoutRequestHandler.class,
	LoginRequestHandler.class,
	NESecurityDeleteUserRequestHandler.class,
	Executor.class,
	NESecurityCreateUserRequestHandler.class })
public class NESecurityCreateUserRequestHandlerTest extends AdapterJUnitBase {
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public NESecurityCreateUserRequestHandlerTest(NE ne) {
		super(ne);
	}

	private NESecurityCreateUserRequestHandler createUserRequestHandler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpConnection() throws Exception {
		initializeSSHMock();

		connect();

		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		createUserRequestHandler = mockRequestHandler(new NESecurityCreateUserRequestHandler());
	}

	@After
	public void teardown() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}
	
	//TODO: test for
	/***
	 * useradd: warning: the home directory already exists.^M
		Not copying any file from skel directory into it.^M
		Creating mailbox file: File exists^M
		[root@0df20bcd599a ~]#
	 * 
	 */

	@Test
	public void createUserTest() throws Exception {
		addCommandMockWithoutPrompt("sudo useradd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "[sudo] password for " + USERNAME + ":\n");
		addCommandMock(ne.getPassword(), Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMockWithoutPrompt("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, "all authentication tokens updated successfully\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		createUserRequestHandler.createUser("user", USERNAME, PASSWORD);
	}

	@Test
	public void createExistingUserTest() throws Exception {
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(StringContains.containsString("Command execution failed"));

		addCommandMock("sudo useradd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX,
				"useradd: user '" + USERNAME + "' already exists\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		createUserRequestHandler.createUser("user", USERNAME, PASSWORD);
	}

	@Test
	public void provideMismatchedPasswordsTest() throws Exception {
		String passwordsNotMatchMessage = "Sorry, passwords do not match.";

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(StringContains.containsString(passwordsNotMatchMessage));

		addCommandMock("sudo useradd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMockWithoutPrompt("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, passwordsNotMatchMessage);
		addCommandMock("sudo userdel " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "[sudo] password for " + USERNAME + ":\n");
		addCommandMock(ne.getPassword(), Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		createUserRequestHandler.createUser("user", USERNAME, PASSWORD);
	}
}
