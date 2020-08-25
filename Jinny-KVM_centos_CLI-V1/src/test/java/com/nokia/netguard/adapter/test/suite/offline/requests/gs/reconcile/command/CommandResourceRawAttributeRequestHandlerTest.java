package com.nokia.netguard.adapter.test.suite.offline.requests.gs.reconcile.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Map;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.resource.CommandResourceRawAttributeResponseData;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CommandResourceRawAttributeResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;
import com.nokia.netguard.adapter.requests.gs.reconcile.command.CommandResourceRawAttributeRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, AdapterConfigurationManager.class,
		CommandResourceRawAttributeRequestHandler.class, LogoutRequestHandler.class, Executor.class })
public class CommandResourceRawAttributeRequestHandlerTest extends AdapterJUnitBase {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public CommandResourceRawAttributeRequestHandlerTest(NE ne) {
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
	public void commandCallTest() throws Exception {
		addCommandMock("echo success" + Constants.PRINT_EXIT_CODE_SUFFIX, "success\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		CommandResourceRawAttributeResponseDataBuilder result = handler.executeCommandResourceRawAttribute(resourceId,
				"name", "value", "echo success");
		CommandResourceRawAttributeResponseData response = result.getResponse();
		Map<String, Object> responseMap = response.getAdapterDataMap();
		Pair<Boolean, String> rawAttributeStatus = (Pair<Boolean, String>) responseMap.get("rawAttributeStatus");

		assertEquals(true, rawAttributeStatus.getFirst());
		assertNull(rawAttributeStatus.getSecond());
	}

	@Test
	public void commandCallWithAttrTest() throws Exception {
		addCommandMock("echo attr=value" + Constants.PRINT_EXIT_CODE_SUFFIX, "success\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);

		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		CommandResourceRawAttributeResponseDataBuilder result = handler.executeCommandResourceRawAttribute(resourceId,
				"name", "value", "echo attr=" + ReconcileCommon.APP_COMMAND_KEYWORD_PLANNED_VALUE);
		CommandResourceRawAttributeResponseData response = result.getResponse();
		Map<String, Object> responseMap = response.getAdapterDataMap();
		Pair<Boolean, String> rawAttributeStatus = (Pair<Boolean, String>) responseMap.get("rawAttributeStatus");

		assertEquals(true, rawAttributeStatus.getFirst());
		assertNull(rawAttributeStatus.getSecond());
	}

	@Test
	public void commandCallFailTest() throws Exception {
		addCommandMock("echo success" + Constants.PRINT_EXIT_CODE_SUFFIX, "ERROR\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);

		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		CommandResourceRawAttributeResponseDataBuilder result = handler.executeCommandResourceRawAttribute(resourceId,
				"name", "value", "echo success");
		CommandResourceRawAttributeResponseData response = result.getResponse();
		Map<String, Object> responseMap = response.getAdapterDataMap();
		Pair<Boolean, String> rawAttributeStatus = (Pair<Boolean, String>) responseMap.get("rawAttributeStatus");

		assertEquals(false, rawAttributeStatus.getFirst());
		assertEquals("ERROR", rawAttributeStatus.getSecond());
	}

	@Test
	public void resourceIdNullTest() throws Exception {
		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: ResourceId should not be null."));
		handler.executeCommandResourceRawAttribute(null, "name", "value", "echo success");
	}
	
	@Test
	public void commandNullTest() throws Exception {
		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");
		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: Command should not be null."));
		handler.executeCommandResourceRawAttribute(resourceId, "name", "value", null);
	}
	
	@Test
	public void attrValNullTest() throws Exception {
		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");
		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: AttributeValue should not be null."));
		handler.executeCommandResourceRawAttribute(resourceId, "name", null, "command");
	}
	
	@Test
	public void attrNameNullTest() throws Exception {
		CommandResourceRawAttributeRequestHandler handler = mockRequestHandler(
				new CommandResourceRawAttributeRequestHandler());
		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");
		
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: AttributeName should not be null."));
		handler.executeCommandResourceRawAttribute(resourceId, null, "val", "command");
	}
}
