package com.nokia.netguard.adapter.test.suite.offline.requests.sso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.xml.sax.SAXException;

import com.nakina.adapter.api.responsedata.sessionmanagement.FingerprintResponseData;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.sso.FingerprintRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	FingerprintRequestHandler.class,
	LogoutRequestHandler.class,
	Executor.class })
public class FingerprintRequestHandlerTest extends AdapterJUnitBase {

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public FingerprintRequestHandlerTest(NE ne) {
		super(ne);
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH, ne.getAdapterDefinitionDir());

		addCommandMock("cat /etc/*-release" + Constants.PRINT_EXIT_CODE_SUFFIX, "Jinny KVM_centos_CLI V1\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("cat /etc/*-release" + Constants.PRINT_EXIT_CODE_SUFFIX, "Jinny KVM_centos_CLI V1\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("cat /etc/*-release" + Constants.PRINT_EXIT_CODE_SUFFIX, "Jinny KVM_centos_CLI V1\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void testFingerPrint() throws RequestFailure, ParserConfigurationException, SAXException, IOException {
		FingerprintRequestHandler handler = mockRequestHandler(new FingerprintRequestHandler());
		
		FingerprintResponseData fingerPrint = handler.getFingerPrintData();
		String neVersion = fingerPrint.getVersion();
		String neModel = fingerPrint.getModel();
		String neVendor = fingerPrint.getVendor();
		assertEquals("Vendor doesn't match", "Jinny", neVendor);
		assertEquals("Model doesn't match", "KVM_centos_CLI", neModel);
		assertEquals("Version doesn't match", "V1", neVersion);
	}
}
