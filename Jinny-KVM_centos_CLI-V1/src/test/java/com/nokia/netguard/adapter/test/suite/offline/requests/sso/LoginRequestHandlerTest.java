package com.nokia.netguard.adapter.test.suite.offline.requests.sso;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	Executor.class,
	LogoutRequestHandler.class })
public class LoginRequestHandlerTest extends AdapterJUnitBase {

	public LoginRequestHandlerTest(NE ne) {
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
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void loginTest() throws Exception {
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());
	}
	
	@Test
	public void changePromptTest() throws Exception {
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());
		
		LoginRequestHandler handler = mockRequestHandler(new LoginRequestHandler());
		addCommandMock("", "");
		addCommandMock("PS1=\"NakinaPrompt>>\"" + Constants.PRINT_EXIT_CODE_SUFFIX, "NakinaPrompt>> \n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		handler.setStandardPrompt(Constants.STANDARD_PROMPT);
		
		addCommandMock("PS1=\"[user@host]:~$ \"" + Constants.PRINT_EXIT_CODE_SUFFIX, "[user@host]:~$ \n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		handler.resetStandardPrompt();

	}
}
