package com.nokia.netguard.adapter.test.suite.realne.requests.gs.resource.retrieval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.nakina.adapter.api.responsedata.inventory.common.RawAttrsResourceResponseData;
import com.nakina.adapter.api.responsedatabuilder.ResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.CredentialsResponseDataBuilder;
import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.resource.retrieval.RetrieveResourceRawAttributesRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LoginRequestHandler;
import com.nokia.netguard.adapter.requests.sso.LogoutRequestHandler;
import com.nokia.netguard.adapter.test.RealNESuite;
import com.nokia.netguard.adapter.test.base.AdapterJUnitBase;
import com.nokia.netguard.adapter.test.base.NE;

import junit.framework.Assert;
import static org.hamcrest.CoreMatchers.containsString;

@PrepareForTest({ RetrieveResourceRawAttributesRequestHandler.class, AdapterConfigurationManager.class, Executor.class,
	LogoutRequestHandler.class, LoginRequestHandler.class })

public class RetrieveResourceRawAttributesRequestHandlerTest extends AdapterJUnitBase {

	private static final String[] TEST_USER_LIST = {"testUser1", "testUser2"};

	private RetrieveResourceRawAttributesRequestHandler retrieveResourceRawAttributesRequestHandler;
	private Executor exec;

	public RetrieveResourceRawAttributesRequestHandlerTest(NE ne) {
		super(ne);
	}

	@Parameters
	public static Collection<NE[]> data() {
		return new RealNESuite().data();
	}

	@Before
	public void setUp() throws Exception {
		initializeSSHMock();
		connect();
		login(LoginRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol(), ne.getAdapterDefinitionDir());
		
		retrieveResourceRawAttributesRequestHandler = (RetrieveResourceRawAttributesRequestHandler) mockRequestHandler(new RetrieveResourceRawAttributesRequestHandler());

		exec = new Executor(retrieveResourceRawAttributesRequestHandler);
		exec.execute(Command.create("useradd testUser1").escalateCommand());
		exec.execute(Command.create("useradd testUser2").escalateCommand());
}

	@After
	public void destroy() throws Exception {
		exec.execute(Command.create("userdel testUser1").escalateCommand());
		exec.execute(Command.create("userdel testUser2").escalateCommand());
		logout(LogoutRequestHandler.class, ManagementProtocol.CLI, ne.getConnectionProtocol());
		disconnect();
		checkIfAllCommandsExecuted();
	}

	@Test
	public void retrivalTest() throws RequestFailure {
		RetrieveResourceRawAttributesRequestHandler handler = mockRequestHandler(new RetrieveResourceRawAttributesRequestHandler());
		List<ResponseDataBuilder> results = handler.getAllRawResourceData();

		assertEquals(29, results.size());

		ResponseDataBuilder managedElement = results.get(0);
		assertEquals("ManagedElement", managedElement.getEntityId());
		assertEquals("Configuration", managedElement.getConfigResourceSubtype());

		checkGSResult(results, TEST_USER_LIST, "ManagedElement");		
	}
	
	private void checkGSResult(List<ResponseDataBuilder> results, String[] testUserList, String entityId) {
		String discoveredUsers = "";

		for (ResponseDataBuilder result : results) {
			if (result instanceof CredentialsResponseDataBuilder) {
				CredentialsResponseDataBuilder userData = (CredentialsResponseDataBuilder)result;
				List<RawAttrsResourceResponseData> builderList = userData.getRawAttrsResponseData();
				for (RawAttrsResourceResponseData builder : builderList) {
					String subType = builder.getConfigResourceSubType();
					if("account".equals(subType)){
						discoveredUsers += builder.getNativeName();
						Trace.info(getClass(), "Adding user "+builder.getNativeName());
					}					
				}				
				for (String testUser : testUserList) {
					
					Trace.info(getClass(), "Checking for user "+testUser);
					assertThat(discoveredUsers, containsString(testUser));
				}
				return;
				
			}
		}

		Assert.fail("Entity: " + entityId + " not found in gs results");

	}
}
