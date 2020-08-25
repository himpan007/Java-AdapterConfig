package com.nokia.netguard.adapter.test.suite.realne.requests.pe;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.pe.NESecurityEscalatePrivilegeRequestHandler;
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
	NESecurityEscalatePrivilegeRequestHandler.class })
public class NESecurityEscalatePrivilegeTest extends AdapterJUnitBase {

	private NESecurityEscalatePrivilegeRequestHandler handler;
	private Executor exec;
	
	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public NESecurityEscalatePrivilegeTest(NE ne) {
		super(ne);
	}


	@Before
	public void setUp() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());

		handler = (NESecurityEscalatePrivilegeRequestHandler) mockRequestHandler(new NESecurityEscalatePrivilegeRequestHandler());
		
		exec = new Executor(handler);
		exec.execute(Command.create("useradd otherUser").escalateCommand());
		exec.execute(Command.create("passwd otherUser").escalateCommand().addPrompt("[N|n]ew.+password:( ?)$"));
		exec.execute(Command.create("otherUserPass1!").addPrompt("([R|r]etype )?[N|n]ew.+password:( ?)$"));
		exec.execute(Command.create("otherUserPass1!"));
	}

	@After
	public void destroy() throws Exception {
		try {
			final StringBuilder whoami = new StringBuilder();
			exec.execute(Command.create("whoami").addCallback(result -> whoami.append(result.getCleanOutput())));
			if (!whoami.toString().contains(ne.getUser())) {
				exec.execute(Command.create("exit"));
			}
			exec.execute(Command.create("userdel otherUser").escalateCommand());
		} catch (RequestFailure e) {
			e.printStackTrace();
		}
		
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void escalatePrivilegeTest() throws Exception {
		String rootPassword = "toor1!";
		handler.escalatePrivilege(rootPassword);
	}

	@Test(expected = RequestFailure.class)
	public void escalatePrivilegeWrongPasswordTest() throws Exception {
		String rootPassword = "wrongPassword";
		handler.escalatePrivilege(rootPassword);
	}

	@Test
	public void escalatePrivilegeForTheDurationOfACommandTest() throws Exception {
		Mockito.doReturn("CLI").when(handler).getRequestInterfaceName();
		String rootPassword = "toor1!";
		String response = handler.escalatePrivilegeForTheDurationOfACommand(rootPassword, "fdisk -l /dev/sda");

		assertThat(response, containsString("Disk"));
	}

	@Test
	public void switchUserTest() throws Exception {
		String otherUser = "otherUser";
		String otherUserPass = "otherUserPass1!";
		handler.switchUser(otherUser, otherUserPass);
	}

	@Test(expected = RequestFailure.class)
	public void switchUserWrongPasswordTest() throws Exception {
		String otherUser = "otherUser";
		String invalidPass = "invalidPass";
		handler.switchUser(otherUser, invalidPass);
	}
}
