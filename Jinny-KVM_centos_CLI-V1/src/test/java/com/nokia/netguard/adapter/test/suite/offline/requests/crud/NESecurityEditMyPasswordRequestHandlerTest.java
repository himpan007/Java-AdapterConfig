package com.nokia.netguard.adapter.test.suite.offline.requests.crud;

import static org.hamcrest.core.StringContains.containsString;

import java.util.Collection;

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
import com.nokia.netguard.adapter.requests.crud.NESecurityEditMyPasswordRequestHandler;
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
public class NESecurityEditMyPasswordRequestHandlerTest extends AdapterJUnitBase {

	private static final String PASSWORD = "newpassword";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public NESecurityEditMyPasswordRequestHandlerTest(NE ne) {
		super(ne);
	}

	NESecurityEditMyPasswordRequestHandler handler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		handler = mockRequestHandler(new NESecurityEditMyPasswordRequestHandler());
	}

	@After
	public void teardown() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void editMyPasswordTest() throws Exception {
		addCommandMockWithoutPrompt("passwd" + Constants.PRINT_EXIT_CODE_SUFFIX, "(current) UNIX password:");
		addCommandMockWithoutPrompt(ne.getPassword(), "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, "all authentication tokens updated successfully\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		handler.editMyPassword("user", ne.getUser(), PASSWORD, ne.getPassword());
	}

	@Test
	public void provideInvalidCurrentPasswordTest() throws Exception {
		String invalidCurrentPasswordMessage = "passwd: Authentication token manipulation error";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(containsString(invalidCurrentPasswordMessage));

		addCommandMockWithoutPrompt("passwd" + Constants.PRINT_EXIT_CODE_SUFFIX, "(current) UNIX password:");
		addCommandMock(ne.getPassword(), invalidCurrentPasswordMessage + "\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		handler.editMyPassword("user", ne.getUser(), PASSWORD, ne.getPassword());
	}

	@Test
	public void provideMismatchedPasswordsTest() throws Exception {
		String passwordsNotMatchMessage = "Sorry, passwords do not match.";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(containsString(passwordsNotMatchMessage));

		addCommandMockWithoutPrompt("passwd" + Constants.PRINT_EXIT_CODE_SUFFIX, "(current) UNIX password:");
		addCommandMockWithoutPrompt(ne.getPassword(), "New password:");
		addCommandMockWithoutPrompt(PASSWORD, "Retype new password:");
		addCommandMock(PASSWORD, passwordsNotMatchMessage);

		handler.editMyPassword("user", ne.getUser(), PASSWORD, ne.getPassword());
	}
}
