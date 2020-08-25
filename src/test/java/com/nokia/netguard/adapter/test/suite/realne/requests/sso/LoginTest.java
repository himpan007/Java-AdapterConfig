package com.nokia.netguard.adapter.test.suite.realne.requests.sso;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.parser.commandline.Constants;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.RealNESuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	Executor.class,
	LogoutRequestHandler.class })
public class LoginTest extends AdapterJUnitBase {

	public LoginTest(NE ne) {
		super(ne);
	}

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();
		connect();
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void loginTest() throws Exception {
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());
	}
	
	@Test
	public void changePromptTest() throws Exception {
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());
		
		LoginRequestHandler handler = mockRequestHandler(new LoginRequestHandler());
		handler.setStandardPrompt(Constants.NAKINA_PROMPT);
		handler.resetStandardPrompt();
	}
}
