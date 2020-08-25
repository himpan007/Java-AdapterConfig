package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.attribute;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CreateResourcesRawAttributesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;
import com.nokia.netguard.adapter.requests.gs.reconcile.attribute.CreateResourcesRawAttributesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, 
	AdapterConfigurationManager.class,
	CreateResourcesRawAttributesRequestHandler.class, 
	LogoutRequestHandler.class, 
	Executor.class })
public class CreateResourcesRawAttributesRequestHandlerTest extends AdapterJUnitBase{
	private static final String ATTRIBUTE = "Empty Passwords";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public CreateResourcesRawAttributesRequestHandlerTest(NE ne) {
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
	public void createAttributeTest() throws Exception {
		addCommandMock("echo \"PermitEmptyPasswords no\" >> /etc/ssh/sshd_config" + Constants.PRINT_EXIT_CODE_SUFFIX, SV_86563r2_output);
		CreateResourcesRawAttributesRequestHandler handler = mockRequestHandler(new CreateResourcesRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);

		
		CreateResourcesRawAttributesResponseDataBuilder result = handler.createResourcesRawAttributes(resourceMap);
		
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseData().getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
	}
	
	@Test
	public void createNotExistingAttributeTest() throws Exception {
		CreateResourcesRawAttributesRequestHandler handler = mockRequestHandler(new CreateResourcesRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		String failAttribute= ATTRIBUTE+"not_existing";
		attributes.put(failAttribute,null);
		
		resourceMap.put(resource, attributes);

		
		CreateResourcesRawAttributesResponseDataBuilder result = handler.createResourcesRawAttributes(resourceMap);
		
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseData().getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Map<String, Pair<Boolean, String>> attributeResult = commandOutput.get(resource);
		assertEquals(failAttribute, attributeResult.keySet().toArray()[0]);
		assertEquals(false , attributeResult.get(failAttribute).getFirst());
		assertEquals(errorOutputForMissedAttribute, attributeResult.get(failAttribute).getSecond());
	}
	
	@Test
	public void createNotExistingResourceTest() throws Exception {
		CreateResourcesRawAttributesRequestHandler handler = mockRequestHandler(new CreateResourcesRawAttributesRequestHandler());
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("not_existing", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);

		
		CreateResourcesRawAttributesResponseDataBuilder result = handler.createResourcesRawAttributes(resourceMap);
		
		Map<ResourceId, Map> commandOutput = (Map<ResourceId, Map>) result.getResponseData().getResponseDataMap().get("resourcesRawAttributesStatus");
		assertEquals(resource, commandOutput.keySet().toArray()[0]);
		
		Map<String, Pair<Boolean, String>> attributeResult = commandOutput.get(resource);
		assertEquals(ATTRIBUTE, attributeResult.keySet().toArray()[0]);
		assertEquals(false , attributeResult.get(ATTRIBUTE).getFirst());
		assertEquals(errorOutputForMissedResource, attributeResult.get(ATTRIBUTE).getSecond());
	}
	
	@Test
	public void attrRulePathNotExistTest() throws Exception {
		CreateResourcesRawAttributesRequestHandler handler = mockRequestHandler(
				new CreateResourcesRawAttributesRequestHandler());		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(matchesRegex(
				".*AttributesRules.csv \\((No such file or directory|The system cannot find the path specified)\\).*"));


		Configuration configurationMock = mock(Configuration.class);
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY)).thenReturn("not_existing_path");
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RAW_ATTRIBUTE_RECONCILE_FILENAME_PROPERTY)).thenReturn("AttributesRules.csv");
				
		PowerMockito.when(AdapterConfigurationManager.getAdapterConfiguration(handler)).thenReturn(configurationMock);
		
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);
		
		handler.createResourcesRawAttributes(resourceMap);
	}
	
	@Test
	public void attrRuleFileNotExistTest() throws Exception {
		CreateResourcesRawAttributesRequestHandler handler = mockRequestHandler(
				new CreateResourcesRawAttributesRequestHandler());		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(matchesRegex(
				".*AttributesRules.csvv \\((No such file or directory|The system cannot find the (file|path) specified)\\).*"));


		Configuration configurationMock = mock(Configuration.class);
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY)).thenReturn("scripts");
		when(configurationMock.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RAW_ATTRIBUTE_RECONCILE_FILENAME_PROPERTY)).thenReturn("AttributesRules.csvv");
				
		PowerMockito.when(AdapterConfigurationManager.getAdapterConfiguration(handler)).thenReturn(configurationMock);
		
		
		Map<ResourceId, Map<String,String>> resourceMap = new HashMap<>();
		ResourceId resource = new ResourceId("SV-86563r2", "RHEL7", "OperatingSystem");
		
		Map<String,String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE,null);
		
		resourceMap.put(resource, attributes);
		
		handler.createResourcesRawAttributes(resourceMap);
	}
	
	private String SV_86563r2_output = "0";
	private String errorOutputForMissedResource = "No Customer Rule found for creating attribute under resource  Resource type: OperatingSystem - Entity type: RHEL7 - Entity ID: not_existing";
	private String errorOutputForMissedAttribute = "No Customer Rule found for creating attribute under resource Resource type: OperatingSystem - Entity type: RHEL7 - Entity ID: SV-86563r2, attribute=Empty Passwordsnot_existing";

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
