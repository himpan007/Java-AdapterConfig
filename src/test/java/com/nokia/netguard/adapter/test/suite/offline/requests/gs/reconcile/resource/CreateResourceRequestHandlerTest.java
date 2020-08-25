package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.resource.CreateResourceResponseData;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CreateResourceResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;
import com.nokia.netguard.adapter.requests.gs.reconcile.resource.CreateResourceRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, AdapterConfigurationManager.class, CreateResourceRequestHandler.class,
		LogoutRequestHandler.class, Executor.class })
public class CreateResourceRequestHandlerTest extends AdapterJUnitBase {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public CreateResourceRequestHandlerTest(NE ne) {
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
	public void createResourceTest() throws Exception {
		String resource1 = "resource1";
		String resource2 = "resource2";
		
		ignoreMockedCommandOrder();
		addCommandMock("echo $ATTRNAME >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo "+resource1+"=res1Val >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo "+resource2+"=res2Val >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		CreateResourceRequestHandler handler = mockRequestHandler(new CreateResourceRequestHandler());

		ResourceId resource = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		Map<String, String> attributes = new HashMap<>();
		attributes.put(resource1, "res1Val");
		attributes.put(resource2, "res2Val");

		ResourceId parentResourceId = new ResourceId("1", "ManagedElement", "ManagedElement");

		CreateResourceResponseDataBuilder result = handler.createResource(resource, attributes, parentResourceId);

		CreateResourceResponseData response = result.getResponse();
		Map<String, Object> responseData = response.getResponseDataMap();
		
		assertEquals(5, responseData.size());
		assertEquals(resource, responseData.get("resourceId"));
		
		Pair<Boolean,String> operationStatus = (Pair<Boolean, String>) responseData.get("resourceOperationStatus");
		assertEquals(true, operationStatus.getFirst());
		assertNull(operationStatus.getSecond());
		
		Map<String, Pair<Boolean,String>> resourceRawAttributesStatus = (Map<String, Pair<Boolean, String>>) responseData.get("resourceRawAttributesStatus");
		assertEquals(2, resourceRawAttributesStatus.size());
	
		Pair<Boolean, String> resource1Pair = resourceRawAttributesStatus.get(resource1);
		assertEquals(true, resource1Pair.getFirst());
		assertNull(resource1Pair.getSecond());

		Pair<Boolean, String> resource2Pair = resourceRawAttributesStatus.get(resource2);
		assertEquals(true, resource2Pair.getFirst());
		assertNull(resource2Pair.getSecond());
	}
	
	@Test
	public void createResourceFirstCommandFailTest() throws Exception {		
		String resource1 = "resource1";
		String resource2 = "resource2";
		
		ignoreMockedCommandOrder();
		addCommandMock("echo $ATTRNAME >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, "ERROR\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		CreateResourceRequestHandler handler = mockRequestHandler(new CreateResourceRequestHandler());

		ResourceId resource = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		Map<String, String> attributes = new HashMap<>();
		attributes.put(resource1, "res1Val");
		attributes.put(resource2, "res2Val");

		ResourceId parentResourceId = new ResourceId("1", "ManagedElement", "ManagedElement");

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(StringContains.containsString("createResourceByRule failed: ERROR"));
		CreateResourceResponseDataBuilder result = handler.createResource(resource, attributes, parentResourceId);
		
	}
	
	@Test
	public void createResourceOneAttrFailTest() throws Exception {
		String resource1 = "resource1";
		String resource2 = "resource2";
		
		ignoreMockedCommandOrder();
		addCommandMock("echo $ATTRNAME >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo "+resource1+"=res1Val >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, "ERROR\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);
		addCommandMock("echo "+resource2+"=res2Val >> /tmp/asdf" + Constants.PRINT_EXIT_CODE_SUFFIX, Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		CreateResourceRequestHandler handler = mockRequestHandler(new CreateResourceRequestHandler());

		ResourceId resource = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		Map<String, String> attributes = new HashMap<>();
		attributes.put(resource1, "res1Val");
		attributes.put(resource2, "res2Val");

		ResourceId parentResourceId = new ResourceId("1", "ManagedElement", "ManagedElement");

		CreateResourceResponseDataBuilder result = handler.createResource(resource, attributes, parentResourceId);

		CreateResourceResponseData response = result.getResponse();
		Map<String, Object> responseData = response.getResponseDataMap();
		
		assertEquals(5, responseData.size());
		assertEquals(resource, responseData.get("resourceId"));
		
		Pair<Boolean,String> operationStatus = (Pair<Boolean, String>) responseData.get("resourceOperationStatus");
		assertEquals(true, operationStatus.getFirst());
		assertNull(operationStatus.getSecond());
		
		Map<String, Pair<Boolean,String>> resourceRawAttributesStatus = (Map<String, Pair<Boolean, String>>) responseData.get("resourceRawAttributesStatus");
		assertEquals(2, resourceRawAttributesStatus.size());
	
		Pair<Boolean, String> resource1Pair = resourceRawAttributesStatus.get(resource1);
		assertEquals(false, resource1Pair.getFirst());
		assertEquals("ERROR",resource1Pair.getSecond());

		Pair<Boolean, String> resource2Pair = resourceRawAttributesStatus.get(resource2);
		assertEquals(true, resource2Pair.getFirst());
		assertNull(resource2Pair.getSecond());
	}
	
	@Test
	public void resourceRulePathNotExistTest() throws Exception {
		String resource1 = "resource1";
		String resource2 = "resource2";
		
		CreateResourceRequestHandler handler = mockRequestHandler(
				new CreateResourceRequestHandler());		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(matchesRegex(
				".*ResourcesRules.csv \\((No such file or directory|The system cannot find the path specified)\\).*"));
		
		
		Configuration configurationMock = mock(Configuration.class);
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY)).thenReturn("not_existing_path");
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RESOURCE_RECONCILE_FILENAME_PROPERTY)).thenReturn("ResourcesRules.csv");
				
		PowerMockito.when(AdapterConfigurationManager.getAdapterConfiguration(handler)).thenReturn(configurationMock);
		
		ResourceId resource = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		Map<String, String> attributes = new HashMap<>();
		attributes.put(resource1, "res1Val");
		attributes.put(resource2, "res2Val");

		ResourceId parentResourceId = new ResourceId("1", "ManagedElement", "ManagedElement");

		handler.createResource(resource, attributes, parentResourceId);
	}
	
	@Test
	public void resourceRuleFileNotExistTest() throws Exception {
		String resource1 = "resource1";
		String resource2 = "resource2";
		
		CreateResourceRequestHandler handler = mockRequestHandler(
				new CreateResourceRequestHandler());		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(matchesRegex(
				".*ResourcesRules.csvv \\((No such file or directory|The system cannot find the (file|path) specified)\\).*"));
		
		
		Configuration configurationMock = mock(Configuration.class);
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY)).thenReturn("scripts");
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RESOURCE_RECONCILE_FILENAME_PROPERTY)).thenReturn("ResourcesRules.csvv");
				
		PowerMockito.when(AdapterConfigurationManager.getAdapterConfiguration(handler)).thenReturn(configurationMock);
		
		ResourceId resource = new ResourceId("resourceID", "subType1", "ResourceTypeTest1");
		Map<String, String> attributes = new HashMap<>();
		attributes.put(resource1, "res1Val");
		attributes.put(resource2, "res2Val");

		ResourceId parentResourceId = new ResourceId("1", "ManagedElement", "ManagedElement");

		handler.createResource(resource, attributes, parentResourceId);
	}

	private Matcher<String> matchesRegex(final String regex) {

		return new TypeSafeMatcher<String>() {

			@Override
			protected boolean matchesSafely(final String item) {
				return item.matches(regex);
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("should match ").appendValue(regex);
			}
		};
	}
}
