package com.nokia.netguard.adapter.test.suite.offline.requests.sso;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.sso.FingerprintRequestHandler;
import com.nokia.netguard.adapter.requests.sso.KeepaliveRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	FingerprintRequestHandler.class,
	KeepaliveRequestHandler.class,
	Executor.class,
	LogoutRequestHandler.class })
public class KeepaliveRequestHandlerTest extends AdapterJUnitBase {

	public KeepaliveRequestHandlerTest(NE ne) {
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

		addCommandMock("pwd" + Constants.PRINT_EXIT_CODE_SUFFIX, "/home/test\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void test() throws RequestFailure {
		KeepaliveRequestHandler kahandler = mockRequestHandler(new KeepaliveRequestHandler());

		kahandler.doKeepalive(ManagementProtocol.CLI, ConnectionProtocol.SSH);
	}
}
