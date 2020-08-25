package com.nokia.netguard.adapter.test.suite.realne.requests.sso;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

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
import com.nokia.netguard.adapter.test.RealNESuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

@PrepareForTest({ LoginRequestHandler.class,
	AdapterConfigurationManager.class,
	FingerprintRequestHandler.class,
	LogoutRequestHandler.class,
	Executor.class })
public class FingerPrintTest extends AdapterJUnitBase {

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	public FingerPrintTest(NE ne) {
		super(ne);
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
	}

	@Test
	public void testFingerPrint() throws RequestFailure, ParserConfigurationException, SAXException, IOException {
		FingerprintRequestHandler handler = (FingerprintRequestHandler)mockRequestHandler(new FingerprintRequestHandler());
		
		FingerprintResponseData fingerPrint = handler.getFingerPrintData();
		String neVersion = fingerPrint.getVersion();
		String neModel = fingerPrint.getModel();
		String neVendor = fingerPrint.getVendor();

		assertEquals("Vendor doesn't match", "Jinny", neVendor);
		assertEquals("Model doesn't match", "KVM_centos_CLI", neModel);
		assertEquals("Version doesn't match", "V1", neVersion);
	}
}
