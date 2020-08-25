package com.nokia.netguard.adapter.test.suite.offline.requests.pe;

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
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.pe.NESecurityEscalatePrivilegeRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	LogoutRequestHandler.class,
	LoginRequestHandler.class,
	Executor.class,
	NESecurityEscalatePrivilegeRequestHandler.class })
public class NESecurityEscalatePrivilegeRequestHandlerTest extends AdapterJUnitBase {

	private static final String USERNAME = "switchuser";
	private static final String PASSWORD = "switchuser";
	private static final String ROOT_PASSWORD = "Jenkins1";
	private NESecurityEscalatePrivilegeRequestHandler handler;

	public NESecurityEscalatePrivilegeRequestHandlerTest(NE ne) {
		super(ne);
	}

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		handler = mockRequestHandler(new NESecurityEscalatePrivilegeRequestHandler());
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void escalatePrivilegeTest() throws Exception {
		addCommandMockWithoutPrompt("su", "Password:");
		addCommandMockWithoutPrompt(ROOT_PASSWORD, "\n[root@ip]#");

		handler.escalatePrivilege(ROOT_PASSWORD);
	}

	@Test(expected = RequestFailure.class)
	public void escalatePrivilegeWrongPasswordTest() throws Exception {
		addCommandMockWithoutPrompt("su", "Password:");
		addCommandMock(PASSWORD + "spill", "su: Authentication failure");

		handler.escalatePrivilege(PASSWORD + "spill");
	}

	@Test
	public void escalatePrivilegeForTheDurationOfACommandTest() throws Exception {
		addCommandMockWithoutPrompt("sudo fdisk -l /dev/sda", "[sudo] password for user:");
		addCommandMock(ne.getPassword(), "Disk /dev/sda: 17.2 GB, 17179869184 bytes, 33554432 sectors\n"
				+ "Units = sectors of 1 * 512 = 512 bytes\n");

		Mockito.doReturn("CLI").when(handler).getRequestInterfaceName();
		String rootPassword = "toor1!";
		String response = handler.escalatePrivilegeForTheDurationOfACommand(rootPassword, "fdisk -l /dev/sda");

		assertThat(response, containsString("Disk"));
	}

	@Test
	public void switchUserTest() throws Exception {
		addCommandMockWithoutPrompt("su - " + USERNAME, "Password:");
		addCommandMockWithoutPrompt(PASSWORD, "[" + USERNAME + "@ip]$");

		handler.switchUser(USERNAME, PASSWORD);
	}

	@Test(expected = RequestFailure.class)
	public void switchUserWrongPasswordTest() throws Exception {
		addCommandMockWithoutPrompt("su - " + USERNAME, "Password:");
		addCommandMock(PASSWORD + "spill", "su: Authentication failure");

		handler.switchUser(USERNAME, PASSWORD + "spill");
	}
}
