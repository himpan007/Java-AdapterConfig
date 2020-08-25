package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.attribute;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.DeleteResourcesRawAttributesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.attribute.DeleteResourcesRawAttributesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, 
					AdapterConfigurationManager.class,
					DeleteResourcesRawAttributesRequestHandler.class, 
					LogoutRequestHandler.class, 
					Executor.class })
public class DeleteResourcesRawAttributesRequestHandlerTest extends AdapterJUnitBase {
	private static final String ATTRIBUTE = "Telnet Server";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public DeleteResourcesRawAttributesRequestHandlerTest(NE ne) {
		super(ne);
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();

		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH,
				ne.getAdapterDefinitionDir());

		ignoreMockedCommandOrder();
	}

	@After
	public void destroy() throws Exception {
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ConnectionProtocol.SSH);
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void SV_86701r1_test() throws Exception {
		addCommandMock("yum -y remove telnet-server" + Constants.PRINT_EXIT_CODE_SUFFIX, SV_86701r1_outputSuccess);
		
		DeleteResourcesRawAttributesRequestHandler handler = mockRequestHandler(new DeleteResourcesRawAttributesRequestHandler());
		
		Map<ResourceId, Set<String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86701r1", "RHEL7", "OperatingSystem");
		
		Set<String> attributes = new HashSet<>();
		attributes.add(ATTRIBUTE);
		
		resourceMap.put(resource, attributes);
		
		DeleteResourcesRawAttributesResponseDataBuilder result = handler.deleteResourcesRawAttributes(resourceMap);
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseData().getResponseDataMap().get("resourcesRawAttributesStatus");
		
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Map<String, Pair<Boolean, String>> attributeResult = commandOutput.get(resource);
		assertEquals(ATTRIBUTE, attributeResult.keySet().toArray()[0]);
		assertEquals(true , attributeResult.get(ATTRIBUTE).getFirst());
		assertEquals(null, attributeResult.get(ATTRIBUTE).getSecond());
	}
	
	
	@Test
	public void SV_86701r1_testFail() throws Exception {
		// output without 'CommandSuccess' strig will be recognized as failed
		addCommandMock("yum -y remove telnet-server" + Constants.PRINT_EXIT_CODE_SUFFIX, SV_86701r1_outputFail);
		
		DeleteResourcesRawAttributesRequestHandler handler = mockRequestHandler(new DeleteResourcesRawAttributesRequestHandler());
		
		Map<ResourceId, Set<String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86701r1", "RHEL7", "OperatingSystem");
		
		Set<String> attributes = new HashSet<>();
		attributes.add(ATTRIBUTE);
		
		resourceMap.put(resource, attributes);
		
		DeleteResourcesRawAttributesResponseDataBuilder result = handler.deleteResourcesRawAttributes(resourceMap);
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseData().getResponseDataMap().get("resourcesRawAttributesStatus");
		
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Map<String, Pair<Boolean, String>> attributeResult = commandOutput.get(resource);
		assertEquals(ATTRIBUTE, attributeResult.keySet().toArray()[0]);
		assertEquals(false , attributeResult.get(ATTRIBUTE).getFirst());
		assertEquals(SV_86701r1_output, attributeResult.get(ATTRIBUTE).getSecond());
	}
		
	private final static String SV_86701r1_output = "Failed to set locale, defaulting to C\n" + 
			"Loaded plugins: fastestmirror, ovl\n" + 
			"Resolving Dependencies\n" + 
			"--> Running transaction check\n" + 
			"---> Package telnet-server.x86_64 1:0.17-64.el7 will be erased\n" + 
			"--> Finished Dependency Resolution\n" + 
			"\n" + 
			"Dependencies Resolved\n" + 
			"\n" + 
			"====================================================================================================================================================================\n" + 
			" Package                                    Arch                                Version                                    Repository                          Size\n" + 
			"====================================================================================================================================================================\n" + 
			"Removing:\n" + 
			" telnet-server                              x86_64                              1:0.17-64.el7                              @base                               55 k\n" + 
			"\n" + 
			"Transaction Summary\n" + 
			"====================================================================================================================================================================\n" + 
			"Remove  1 Package\n" + 
			"\n" + 
			"Installed size: 55 k\n" + 
			"Downloading packages:\n" + 
			"Running transaction check\n" + 
			"Running transaction test\n" + 
			"Transaction test succeeded\n" + 
			"Running transaction\n" + 
			"  Erasing    : 1:telnet-server-0.17-64.el7.x86_64                                                                                                               1/1 \n" + 
			"  Verifying  : 1:telnet-server-0.17-64.el7.x86_64                                                                                                               1/1 \n" + 
			"\n" + 
			"Removed:\n" + 
			"  telnet-server.x86_64 1:0.17-64.el7                                                                                                                                \n" + 
			"\n" + 
			"Complete!"; 
	
	private final static String SV_86701r1_outputFail = SV_86701r1_output + "\n22";
	private final static String SV_86701r1_outputSuccess = SV_86701r1_output + "\n0";
}
