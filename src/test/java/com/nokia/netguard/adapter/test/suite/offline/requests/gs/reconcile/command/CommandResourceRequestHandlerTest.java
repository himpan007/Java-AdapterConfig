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

import com.nakina.adapter.api.responsedata.inventory.resource.CommandResourceResponseData;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CommandResourceResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.util.Pair;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.command.CommandResourceRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.OfflineSuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.offline.requests.Constants;

@PrepareForTest({ LoginRequestHandler.class, AdapterConfigurationManager.class, CommandResourceRequestHandler.class,
		LogoutRequestHandler.class, Executor.class })
public class CommandResourceRequestHandlerTest extends AdapterJUnitBase {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Parameters
	public static Collection<NE[]> data() {
		return new OfflineSuite().data();
	}

	public CommandResourceRequestHandlerTest(NE ne) {
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
	public void singleCommandSuccessTest() throws Exception {
		addCommandMock("echo success" + Constants.PRINT_EXIT_CODE_SUFFIX, "success\n" + Constants.SUCCESSFUL_EXECUTION_EXIT_CODE);
		CommandResourceRequestHandler handler = mockRequestHandler(new CommandResourceRequestHandler());

		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		CommandResourceResponseDataBuilder result = handler.executeCommandResource(resourceId, "echo success");
		CommandResourceResponseData response = result.getResponse();
		Map<String, Object> responseMap = response.getAdapterDataMap();
		Pair<Boolean, String> rawAttributeStatus = (Pair<Boolean, String>) responseMap.get("resourceStatus");

		assertEquals(true, rawAttributeStatus.getFirst());
		assertNull(rawAttributeStatus.getSecond());
	}

	@Test
	public void singleCommandFailTest() throws Exception {
		addCommandMock("echo success" + Constants.PRINT_EXIT_CODE_SUFFIX, "ERROR\n" + Constants.UNSUCCESSFUL_EXECUTION_EXIT_CODE);
		CommandResourceRequestHandler handler = mockRequestHandler(new CommandResourceRequestHandler());

		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		CommandResourceResponseDataBuilder result = handler.executeCommandResource(resourceId, "echo success");
		CommandResourceResponseData response = result.getResponse();
		Map<String, Object> responseMap = response.getAdapterDataMap();
		Pair<Boolean, String> rawAttributeStatus = (Pair<Boolean, String>) responseMap.get("resourceStatus");

		assertEquals(false, rawAttributeStatus.getFirst());
		assertEquals("ERROR", rawAttributeStatus.getSecond());
	}

	@Test
	public void singleCommandNullTest() throws Exception {
		CommandResourceRequestHandler handler = mockRequestHandler(new CommandResourceRequestHandler());

		ResourceId resourceId = new ResourceId("resourceID", "subType", "ResourceType");

		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: Command should not be null."));
		handler.executeCommandResource(resourceId, null);
	}

	@Test
	public void singleResourceIdNullTest() throws Exception {
		CommandResourceRequestHandler handler = mockRequestHandler(new CommandResourceRequestHandler());
		expectedException.expect(RequestFailure.class);
		expectedException.expectMessage(
				StringContains.containsString("Request Arguments Validation Failed: ResourceId should not be null."));
		handler.executeCommandResource(null, "command");
	}
}
