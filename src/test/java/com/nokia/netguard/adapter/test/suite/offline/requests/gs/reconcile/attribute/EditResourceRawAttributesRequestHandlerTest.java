package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.resource.EditResourcesRawAttributesResponseData;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.attribute.EditResourceRawAttributesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, 
	AdapterConfigurationManager.class,
	EditResourceRawAttributesRequestHandler.class,
	LogoutRequestHandler.class, 
	Executor.class })
public class EditResourceRawAttributesRequestHandlerTest extends AdapterJUnitBase {	
	private static final String ATTRIBUTE = "Empty Passwords";

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public EditResourceRawAttributesRequestHandlerTest(NE ne) {
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
	public void setEmptyPasswordsAttributeTest() throws Exception {
		addCommandMock("sed -r 's/^PermitEmptyPasswords.*/PermitEmptyPasswords no/' -i'.SV-86563r2' /etc/ssh/sshd_config" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		
		EditResourceRawAttributesRequestHandler handler = mockRequestHandler(new EditResourceRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);
		
		EditResourcesRawAttributesResponseData result = handler.editResourcesRawAttributes(resourceMap);
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(1, commandOutput.size());
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Collection<Map> attributesResultCollection = commandOutput.values();
		assertEquals(1, attributesResultCollection.size());
		attributesResultCollection.forEach(map -> {map.keySet().forEach(attribute -> {assertEquals(ATTRIBUTE, attribute);});});
		
		attributesResultCollection.forEach(map -> {
			map.values()
			.forEach(value -> {
				assertEquals(true, ((Pair)value).getFirst());
				assertNull(((Pair)value).getSecond());
				});});		
	}

	
	@Test
	public void setEmptyPasswordsResourceTestNotExistingResource() throws Exception {
		EditResourceRawAttributesRequestHandler handler = mockRequestHandler(new EditResourceRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("not_existing", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);
		
		EditResourcesRawAttributesResponseData result = handler.editResourcesRawAttributes(resourceMap);
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(1, commandOutput.size());
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Collection<Map> attributesResultCollection = commandOutput.values();
		assertEquals(1, attributesResultCollection.size());
		attributesResultCollection.forEach(map -> {map.keySet().forEach(attribute -> {assertEquals(ATTRIBUTE, attribute);});});
		
		attributesResultCollection.forEach(map -> {
			map.values()
			.forEach(value -> {
				assertEquals(false, ((Pair)value).getFirst());
				assertNotNull(((Pair)value).getSecond());
				assertEquals("No Customer Rule found for updating attributes under resource  Resource type: OperatingSystem - Entity type: RHEL7 - Entity ID: not_existing", ((Pair)value).getSecond());
				});});		
	}
	
	@Test
	public void setEmptyPasswordsAttributeTestNotExistingResource() throws Exception {
		EditResourceRawAttributesRequestHandler handler = mockRequestHandler(new EditResourceRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE + "not_exist",null);
		
		resourceMap.put(resource, attributes);
		
		EditResourcesRawAttributesResponseData result = handler.editResourcesRawAttributes(resourceMap);
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(1, commandOutput.size());
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Collection<Map> attributesResultCollection = commandOutput.values();
		assertEquals(1, attributesResultCollection.size());
		attributesResultCollection.forEach(map -> {map.keySet().forEach(attribute -> {assertEquals(ATTRIBUTE  +"not_exist", attribute);});});
		
		attributesResultCollection.forEach(map -> {
			map.values()
			.forEach(value -> {
				assertEquals(false, ((Pair)value).getFirst());
				assertNotNull(((Pair)value).getSecond());
				assertEquals("No User Rules to support EditAttributes for resource Resource type: OperatingSystem - Entity type: RHEL7 - Entity ID: SV-86563r2, attribute=Empty Passwordsnot_exist", ((Pair)value).getSecond());
				});});		
	}
}
