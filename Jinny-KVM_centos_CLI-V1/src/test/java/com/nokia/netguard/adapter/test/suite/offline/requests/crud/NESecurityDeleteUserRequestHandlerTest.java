package com.nokia.netguard.adapter.test.suite.offline.requests.crud;

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
import com.nokia.netguard.adapter.cli.Executor;
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
	Executor.class,
	NESecurityDeleteUserRequestHandler.class })
public class NESecurityDeleteUserRequestHandlerTest extends AdapterJUnitBase {

	private static final String USERNAME = "user";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public NESecurityDeleteUserRequestHandlerTest(NE ne) {
		super(ne);
	}

	private NESecurityDeleteUserRequestHandler deleteUserRequestHandler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpConnection() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		deleteUserRequestHandler = mockRequestHandler(new NESecurityDeleteUserRequestHandler());
	}

	@After
	public void teardown() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void deleteUserTest() throws Exception {
		addCommandMock("sudo userdel " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "[sudo] password for " + USERNAME + ":\n");
		addCommandMock(ne.getPassword(), Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		deleteUserRequestHandler.deleteUser("user", USERNAME);
	}

	@Test
	public void deleteNotExistingUserTest() throws Exception {
		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(StringContains.containsString("Command execution failed"));

		addCommandMock("sudo userdel " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX,
				"userdel: user '" + USERNAME + "' does not exist\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		deleteUserRequestHandler.deleteUser("user", USERNAME);
	}
}
