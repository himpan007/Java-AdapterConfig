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
import com.nokia.netguard.adapter.requests.crud.NESecurityDeleteUserRequestHandler;
import com.nokia.netguard.adapter.requests.crud.NESecurityEditMyPasswordRequestHandler;
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
public class NESecurityEditMyPasswordTest extends AdapterJUnitBase {

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public NESecurityEditMyPasswordTest(NE ne) {
		super(ne);
	}

	NESecurityEditMyPasswordRequestHandler handler;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Executor exec;

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());

		handler = (NESecurityEditMyPasswordRequestHandler) mockRequestHandler(new NESecurityEditMyPasswordRequestHandler());
		
		exec = new Executor(handler);
	}

	@After
	public void teardown() throws Exception {
		exec.execute(Command.create("passwd").addPrompt("\\(current\\) UNIX password:"));
		exec.execute(Command.create("newPassword1!").addPrompt("[N|n]ew.+password:( ?)$"));
		exec.execute(Command.create(ne.getPassword()).addPrompt("([R|r]etype )?[N|n]ew.+password:( ?)$"));
		exec.execute(Command.create(ne.getPassword()));

		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void editMyPasswordTest() throws Exception {
		String newPassword = "newPassword1!";
		handler.editMyPassword("User Accounts", ne.getUser(), newPassword, ne.getPassword());
	}

	@Test
	public void provideInvalidCurrentPasswordTest() throws Exception {
		String invalidCurrentPasswordMessage = "passwd: Authentication token manipulation error";
		
		String newPassword = "newPassword1!";
		String invalidPassword = "invalidPassword";
		
		expectedException.expect(RequestFailure.class);
	    expectedException.expectMessage(StringContains.containsString(invalidCurrentPasswordMessage));

		handler.editMyPassword("User Accounts", ne.getUser(), newPassword, invalidPassword);
	}

}
