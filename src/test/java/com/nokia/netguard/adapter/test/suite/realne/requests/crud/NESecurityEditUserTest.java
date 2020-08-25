package com.nokia.netguard.adapter.test.suite.realne.requests.crud;

import static org.mockito.Mockito.doReturn;

import java.util.Collection;

import org.apache.logging.log4j.Logger;
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
import com.nokia.netguard.adapter.requests.crud.NESecurityEditUserRequestHandler;
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
	NESecurityEditUserRequestHandler.class,
	Logger.class})
public class NESecurityEditUserTest extends AdapterJUnitBase {

	private NESecurityEditUserRequestHandler handler;
	private Executor exec;

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public NESecurityEditUserTest(NE ne) {
		super(ne);
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void before() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());

		handler = (NESecurityEditUserRequestHandler) mockRequestHandler(new NESecurityEditUserRequestHandler());

		exec = new Executor(handler);
		exec.execute(Command.create("useradd changePassUser").escalateCommand());
	}

	@After
	public void after() throws Exception {
		exec.execute(Command.create("userdel changePassUser").escalateCommand());
		
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void editUserTest() throws Exception {
		String username = "changePassUser";
		String newPassword = "newPassword1!";
		doReturn(newPassword).when(handler).getPassword();
		handler.editUser("User Accounts", username);
	}

	@Test
	public void changePasswordOfNotExistingUserTest() throws Exception {
		String username = "nonexistentUser";
		String newPassword = "newPassword1!";
		doReturn(newPassword).when(handler).getPassword();
		String userDoesNotExistMessage = ".*passwd: Unknown user name '" + username + "'.*|.*passwd: user '"+username+"' does not exist.*";

		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(matcher(userDoesNotExistMessage));

		handler.editUser("User Accounts", username);
	}
	
}
