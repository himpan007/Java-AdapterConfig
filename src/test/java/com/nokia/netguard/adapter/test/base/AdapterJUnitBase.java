package com.nokia.netguard.adapter.test.base;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.nakina.adapter.api.requestarguments.sessionmanagement.AuthenticationArguments;
import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.LoginStrategyBase;
import com.nakina.adapter.base.agent.api.associationmgmt.LogoutStrategyBase;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.CLICommand;
import com.nakina.adapter.base.agent.api.base.parser.commandline.WaitForStringCommand;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.test.ngagent.AdapterConfigurationManagerMock;
import com.nokia.netguard.adapter.test.ngagent.AdapterConfigurationMock;
import com.nokia.netguard.adapter.test.ngagent.CLICommandMock;
import com.nokia.netguard.adapter.test.ngagent.CommandAndResponseList;
import com.nokia.netguard.adapter.test.ngagent.session.SessionContext;
import com.nokia.netguard.adapter.test.ngagent.session.SessionManagerMock;
import com.nokia.netguard.adapter.test.ngagent.session.SshSessionManagerMock;
import com.nokia.netguard.adapter.test.ngagent.session.TelnetSessionManagerMock;
import com.nokia.netguard.adapter.test.suite.TestSuite;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore({ "javax.management.*", "org.apache.sshd.*", "com.jcraft.*" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdapterJUnitBase {
	private boolean adapterConfigurationMocked;
	private boolean waitForStringCommandMocked;
	private boolean cliCommandMocked;

	private CommandAndResponseList commandAndResponse;

	protected LoginStrategyBase loginHandler;
	protected LogoutStrategyBase logoutHandler;
	protected SessionManagerMock manager;
	protected NE ne;
	private boolean adapterConfigurationManagerMocked;
	
	@Rule
	public TestTracer testTrace;

	public AdapterJUnitBase(NE ne) {
		this.ne = ne;
		testTrace = new TestTracer(ne);
	}

	protected void initializeSSHMock() throws IOException {
		if (ne.getIp().contains(TestSuite.OFFLINE_TEST)) {
			commandAndResponse = new CommandAndResponseList();
			commandAndResponse.addCommandAndResponse("", "\n");
		}
	}

	protected void connect() throws IOException {
		if (!ne.getIp().contains(TestSuite.OFFLINE_TEST)) {
			if (ne.getConnectionProtocol().equals(ConnectionProtocol.SSH)) {
				manager = new SshSessionManagerMock(ne);
			} else if (ne.getConnectionProtocol().equals(ConnectionProtocol.TELNET)){
				manager = new TelnetSessionManagerMock(ne);
			}
			SessionContext.setManager(manager);
			manager.connect();
		}
	}

	protected void disconnect() {
		if (!ne.getIp().contains(TestSuite.OFFLINE_TEST)) {
			if (manager != null) {
				manager.disconnect();
				manager = null;
			}
		}
	}

	protected LoginStrategyBase login(Class<? extends LoginStrategyBase> handler, ManagementProtocol mProtocol,
			ConnectionProtocol cProtocol, String adapterDefinitionDir) throws Exception {
		Constructor<? extends LoginStrategyBase> defaultConstructor = handler.getConstructor();
		loginHandler = mockRequestHandler(defaultConstructor.newInstance());

		mockAdapterConfigurationManager();
		mockAdapterConfiguration(adapterDefinitionDir);
		mockWaitForStringCommand();
		mockCliCommand();

		addCommandMock("pwd", "/home");

		AuthenticationArguments credentials = new AuthenticationArguments(ne.getUser(), ne.getPassword());
		loginHandler.doLogin(credentials, mProtocol, cProtocol);

		return loginHandler;
	}

	protected void logout(Class<? extends LogoutStrategyBase> handler, ManagementProtocol mProtocol,
			ConnectionProtocol cProtocol) throws Exception {
		Constructor<? extends LogoutStrategyBase> defaultConstructor = handler.getConstructor();
		logoutHandler = defaultConstructor.newInstance();
		AuthenticationArguments credentials = new AuthenticationArguments(ne.getUser(), ne.getPassword());

		LogoutStrategyBase logout = mockRequestHandler(logoutHandler);

		addCommandMock("logout", "");

		logout.doLogout(credentials, ManagementProtocol.CLI, ConnectionProtocol.SSH);
	}

	protected void ignoreMockedCommandOrder() {
		if (commandAndResponse != null) {
			commandAndResponse.setIgnoreCommandOrder(true);
		}
	}
	
	protected void addCommandMock(String command, String response) throws Exception {
		if (commandAndResponse != null) {
			commandAndResponse.addCommandAndResponse(command, response + "\n" + ne.getOfflineNEPrompt());
		}
	}

	protected void addCommandMockWithoutPrompt(String command, String response) throws Exception {
		if (commandAndResponse != null) {
			commandAndResponse.addCommandAndResponse(command, response);
		}
	}

	protected void checkIfAllCommandsExecuted() {
		if (commandAndResponse != null) {
			assertTrue(commandAndResponse.allCommandsExecuted());
		}
	}

	public <T extends BaseCommand> T mockRequestHandler(T requestHandler) {
		T handler = PowerMockito.spy(requestHandler);
		
		Mockito.doReturn(ne.getIp()).when(handler).getNeIdentifier();
		Mockito.doReturn(requestHandler.getClass().getSimpleName()).when(handler).getRequestName();
		
		AuthenticationArguments authArgs = Mockito.mock(AuthenticationArguments.class);
		Mockito.doReturn(ne.getPassword()).when(authArgs).getPassword();
		Mockito.doReturn(authArgs).when(handler).getAuthenticationArguments(any());

		Mockito.doReturn(ManagementProtocol.CLI).when(handler).getManagementProtocol();

		return handler;
	}

	public void mockCliCommand() throws Exception {
		if (!cliCommandMocked) {
			whenNew(CLICommand.class)
					.withArguments(any(), any(String.class), any(String.class), any(String.class), any(Integer.class))
					.thenAnswer(new Answer<CLICommand>() {

						@Override
						public CLICommand answer(InvocationOnMock invocation) throws Throwable {
							Object[] args = invocation.getArguments();
							CLICommandMock commandMockImplementation = new CLICommandMock(commandAndResponse,
									(String) args[2], (String) args[3], ne.getIp());
							CLICommand commandMock = mock(CLICommand.class);
							when(commandMock.removeEchoAndPrompt()).thenAnswer(answer->commandMockImplementation.removeEchoAndPrompt());
							when(commandMock.send()).thenAnswer(new Answer<String>() {

								@Override
								public String answer(InvocationOnMock invocation) throws Throwable {
									return commandMockImplementation.send();
								}
							});
							return commandMock;
						}
					});

			whenNew(CLICommand.class).withArguments(any(), any(String.class), any(String.class), any(String.class),
					any(String.class), any(Integer.class)).thenAnswer(new Answer<CLICommand>() {

						@Override
						public CLICommand answer(InvocationOnMock invocation) throws Throwable {
							Object[] args = invocation.getArguments();
							CLICommandMock commandMockImplementation = new CLICommandMock(commandAndResponse,
									(String) args[2], (String) args[4], ne.getIp());
							CLICommand commandMock = mock(CLICommand.class);
							when(commandMock.removeEchoAndPrompt()).thenAnswer(answer->commandMockImplementation.removeEchoAndPrompt());
							when(commandMock.send()).thenAnswer(new Answer<String>() {

								@Override
								public String answer(InvocationOnMock invocation) throws Throwable {
									return commandMockImplementation.send();
								}
							});
							return commandMock;
						}
					});

			whenNew(CLICommand.class).withArguments(any(), any(String.class), any(String.class), any(String.class)
					).thenAnswer(new Answer<CLICommand>() {

						@Override
						public CLICommand answer(InvocationOnMock invocation) throws Throwable {
							Object[] args = invocation.getArguments();
							CLICommandMock commandMockImplementation = new CLICommandMock(commandAndResponse,
									(String) args[2], (String) args[3], ne.getIp());
							CLICommand commandMock = mock(CLICommand.class);
							when(commandMock.removeEchoAndPrompt()).thenAnswer(answer->commandMockImplementation.removeEchoAndPrompt());
							when(commandMock.send()).thenAnswer(new Answer<String>() {

								@Override
								public String answer(InvocationOnMock invocation) throws Throwable {
									return commandMockImplementation.send();
								}
							});
							return commandMock;
						}
					});
			
			cliCommandMocked = true;
		}
	}

	public void mockWaitForStringCommand() throws Exception {
		if (!waitForStringCommandMocked) {
			WaitForStringCommand waitForStringCommandMock = mock(WaitForStringCommand.class);
			when(waitForStringCommandMock.waitForAutonomousString(any(String.class), any(Integer.class)))
					.thenAnswer(new Answer<String>() {

						@Override
						public String answer(InvocationOnMock invocation) throws Throwable {
							return new CLICommandMock(commandAndResponse, "", invocation.getArgumentAt(0, String.class), ne.getIp()).send();
						}
					});

			whenNew(WaitForStringCommand.class).withAnyArguments().thenReturn(waitForStringCommandMock);
			waitForStringCommandMocked = true;
		}
	}

	public void mockAdapterConfiguration(String adapterDefinitionDir) throws Exception {
		if (!adapterConfigurationMocked) {
			AdapterConfigurationMock adcMock = new AdapterConfigurationMock(adapterDefinitionDir);
			AdapterConfiguration mock = mock(AdapterConfiguration.class);
			when(mock.getNeLineTermination()).thenReturn(adcMock.getNeLineTermination());
			when(mock.getStringValue(any(String.class), any(String.class))).thenAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					return adcMock.getStringValue((String) args[0], (String) args[1]);
				}
			});
			when(mock.getIntegerValue(any(String.class), any(Integer.class))).thenAnswer(new Answer<Integer>() {
				@Override
				public Integer answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					return adcMock.getIntegerValue((String) args[0], (Integer) args[1]);
				}
			});
			when(mock.getBooleanValue(any(String.class), any(Boolean.class))).thenAnswer(new Answer<Boolean>() {
				@Override
				public Boolean answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					return adcMock.getBooleanValue((String) args[0], (Boolean) args[1]);
				}
			});

			whenNew(AdapterConfiguration.class).withAnyArguments().thenReturn(mock);
			adapterConfigurationMocked = true;
		}
	}

	public void mockAdapterConfigurationManager() throws Exception {
		if (!adapterConfigurationManagerMocked) {
			PowerMockito.mockStatic(AdapterConfigurationManager.class);
			when(AdapterConfigurationManager.getAdapterConfiguration(any()))
					.thenReturn(AdapterConfigurationManagerMock.getAdapterConfiguration());
			adapterConfigurationManagerMocked = true;
		}
	}
	
	public Matcher<String> matcher(String regex) {
		return new BaseMatcher<String>() {

			@Override
			public boolean matches(Object arg0) {
				Pattern pat = Pattern.compile(regex);
				return pat.matcher((String)arg0).find();
			}

			@Override
			public void describeTo(Description arg0) {}
			
		};
	}

}