package com.nokia.netguard.adapter.test.suite.offline.requests.crud;

import static org.mockito.Mockito.doReturn;

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
import com.nokia.netguard.adapter.requests.crud.NESecurityEditUserRequestHandler;
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
	NESecurityEditUserRequestHandler.class })
public class NESecurityEditUserRequestHandlerTest extends AdapterJUnitBase {

	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public NESecurityEditUserRequestHandlerTest(NE ne) {
		super(ne);
	}

	private NESecurityEditUserRequestHandler handler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		handler = mockRequestHandler(new NESecurityEditUserRequestHandler());
		doReturn(PASSWORD).when(handler).getPassword();
	}

	@After
	public void teardown() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void editUserTest() throws Exception {
		addCommandMockWithoutPrompt("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "[sudo] password for " + USERNAME + ":");
		addCommandMockWithoutPrompt(ne.getPassword(), "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, "all authentication tokens updated successfully\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		
		handler.editUser("user", USERNAME);
	}

	@Test
	public void changePasswordOfNotExistingUserTest() throws Exception {
		String userDoesNotExistMessage = "passwd: Unknown user name '" + USERNAME + "'.";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(StringContains.containsString(userDoesNotExistMessage));

		addCommandMock("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX,
				userDoesNotExistMessage + "\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		handler.editUser("user", USERNAME);
	}

	@Test
	public void provideMismatchedPasswordsTest() throws Exception {
		String passwordsNotMatchMessage = "Sorry, passwords do not match.";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(StringContains.containsString(passwordsNotMatchMessage));

		addCommandMockWithoutPrompt("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX, "[sudo] password for " + USERNAME + ":");
		addCommandMockWithoutPrompt(ne.getPassword(), "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, passwordsNotMatchMessage);

		handler.editUser("user", USERNAME);
	}
	
	@Test
	public void userNotExistTest() throws Exception {
		String passwordsNotMatchMessage = "passwd: Unknown user name '" + USERNAME + "'.";

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(StringContains.containsString(passwordsNotMatchMessage));

		addCommandMockWithoutPrompt("sudo passwd " + USERNAME + Constants.PRINT_EXIT_CODE_SUFFIX,
				"passwd: Unknown user name '" + USERNAME + "'.\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		handler.editUser("user", USERNAME);
	}
}
