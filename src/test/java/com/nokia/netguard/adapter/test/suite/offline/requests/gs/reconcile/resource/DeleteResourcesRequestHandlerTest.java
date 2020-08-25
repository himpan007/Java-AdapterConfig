package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.resource.DeleteResourcesResponseData;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.DeleteResourcesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.resource.DeleteResourcesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, 
	AdapterConfigurationManager.class, 
	DeleteResourcesRequestHandler.class,
	LogoutRequestHandler.class,  
	Executor.class })
public class DeleteResourcesRequestHandlerTest extends AdapterJUnitBase {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public DeleteResourcesRequestHandlerTest(NE ne) {
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
	public void deleteResourceTest() throws Exception {		
		ignoreMockedCommandOrder();
		addCommandMock("echo deleteResource1" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo deleteResource2Sub2" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo deleteResource2" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		DeleteResourcesRequestHandler handler = mockRequestHandler(new DeleteResourcesRequestHandler());

		ResourceId resourceId1 = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		ResourceId resourceId2 = new ResourceId("resourceID2", "subType1", "ResourceTypeTest1");
		ResourceId resource2subtype2 = new ResourceId("resourceID2", "subType2", "ResourceTypeTest1");
		Set<ResourceId> resources = new HashSet<>();
		resources.add(resourceId1);
		resources.add(resourceId2);
		resources.add(resource2subtype2);
		
		DeleteResourcesResponseDataBuilder result = handler.deleteResources(resources);

		DeleteResourcesResponseData responseData = result.getResponse();
		
		Map<String, Object> responseDataMap = responseData.getResponseDataMap();
		
		assertEquals(3, responseDataMap.size());
		
		Map<ResourceId, Pair<Boolean, String>> resourcesOperationStatusMap = (Map<ResourceId, Pair<Boolean, String>>) responseDataMap.get("resourcesOperationStatus");
		
		assertEquals(3, resourcesOperationStatusMap.size());
		
		Pair<Boolean, String> resourceResultPair = resourcesOperationStatusMap.get(resourceId1);
		assertEquals(true, resourceResultPair.getFirst());
		assertNull(resourceResultPair.getSecond());
		
		resourceResultPair = resourcesOperationStatusMap.get(resourceId2);
		assertEquals(true, resourceResultPair.getFirst());
		assertNull(resourceResultPair.getSecond());
		
		resourceResultPair = resourcesOperationStatusMap.get(resource2subtype2);
		assertEquals(true, resourceResultPair.getFirst());
		assertNull(resourceResultPair.getSecond());
	}
	
	@Test
	public void deleteResourceNotExistingTest() throws Exception {		
		DeleteResourcesRequestHandler handler = mockRequestHandler(new DeleteResourcesRequestHandler());

		ResourceId fake = new ResourceId("resourceID_Not_Existing", "fake", "fake");
		Set<ResourceId> resources = new HashSet<>();
		resources.add(fake);
		
		DeleteResourcesResponseDataBuilder result = handler.deleteResources(resources);

		DeleteResourcesResponseData responseData = result.getResponse();
		
		Map<String, Object> responseDataMap = responseData.getResponseDataMap();
		
		assertEquals(3, responseDataMap.size());
		Map<ResourceId, Pair<Boolean, String>> resourcesOperationStatusMap = (Map<ResourceId, Pair<Boolean, String>>) responseDataMap.get("resourcesOperationStatus");
		
		assertEquals(1, resourcesOperationStatusMap.size());
		
		Pair<Boolean, String> resourceResultPair = resourcesOperationStatusMap.get(fake);
		assertEquals(false, resourceResultPair.getFirst());
		assertEquals("No Customer Rule found for deleting resource Resource type: fake - Entity type: fake - Entity ID: resourceID_Not_Existing", resourceResultPair.getSecond());
	}
	
	@Test
	public void deleteResourceOneFailTest() throws Exception {		
		ignoreMockedCommandOrder();
		addCommandMock("echo deleteResource1" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo deleteResource2Sub2" + Constants.PRINT_EXIT_CODE_SUFFIX, "ERROR\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo deleteResource2" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		DeleteResourcesRequestHandler handler = mockRequestHandler(new DeleteResourcesRequestHandler());

		ResourceId resourceId1 = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		ResourceId resourceId2 = new ResourceId("resourceID2", "subType1", "ResourceTypeTest1");
		ResourceId resource2subtype2 = new ResourceId("resourceID2", "subType2", "ResourceTypeTest1");
		Set<ResourceId> resources = new HashSet<>();
		resources.add(resourceId1);
		resources.add(resourceId2);
		resources.add(resource2subtype2);
		
		DeleteResourcesResponseDataBuilder result = handler.deleteResources(resources);

		DeleteResourcesResponseData responseData = result.getResponse();
		
		Map<String, Object> responseDataMap = responseData.getResponseDataMap();
		
		assertEquals(3, responseDataMap.size());
		
		Map<ResourceId, Pair<Boolean, String>> resourcesOperationStatusMap = (Map<ResourceId, Pair<Boolean, String>>) responseDataMap.get("resourcesOperationStatus");
		
		assertEquals(3, resourcesOperationStatusMap.size());
		
		Pair<Boolean, String> resourceResultPair = resourcesOperationStatusMap.get(resourceId1);
		assertEquals(true, resourceResultPair.getFirst());
		assertNull(resourceResultPair.getSecond());
		
		resourceResultPair = resourcesOperationStatusMap.get(resourceId2);
		assertEquals(true, resourceResultPair.getFirst());
		assertNull(resourceResultPair.getSecond());
		
		resourceResultPair = resourcesOperationStatusMap.get(resource2subtype2);
		assertEquals(false, resourceResultPair.getFirst());
		assertEquals("ERROR", resourceResultPair.getSecond());
	}
	
}
