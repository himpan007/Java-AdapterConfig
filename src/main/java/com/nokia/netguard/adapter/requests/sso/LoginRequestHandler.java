package com.nokia.netguard.adapter.requests.sso;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;

import com.nakina.adapter.api.agent.commandmanager.SessionAddressHolder;
import com.nakina.adapter.api.agent.commandmanager.cli.CLICommandManager;
import com.nakina.adapter.api.requestarguments.sessionmanagement.AuthenticationArguments;
import com.nakina.adapter.api.responsedata.sessionmanagement.AuthenticationResponseData;
import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.LoginStrategyBase;
import com.nakina.adapter.base.agent.api.base.CLICommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.base.parser.commandline.WaitForStringCommand;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.configuration.Constants;


public class LoginRequestHandler extends LoginStrategyBase {

	private AuthenticationResponseData authenticationResponseData = null;

	protected String  nePasswordMaskCharacters        = null;
	protected String  neLineTermination               = null;
	protected Integer cliCommandTimeoutInMilliseconds = null;
	protected Integer cliWaitForPromptInMilliseconds  = null;
	protected String  cliLoginPrompt                  = null;
	protected String  cliPasswordPrompt               = null;
	protected String  cliInitialPromptString          = null;
	protected String  sshSimpleCommand                = null;
	protected String  logPrefix                       = Constants.LOG_PREFIX;
	protected String  originalPrompt                  = null;
	protected String  standardPrompt                  = null;
	protected boolean supportsStandardPrompt          = false;
	protected boolean isGateway                       = Constants.IS_GATEWAY;

	@Override
	public AuthenticationResponseData doLogin(AuthenticationArguments credentials,
			ManagementProtocol managementProtocol, ConnectionProtocol connectionProtocol) throws RequestFailure {

		AdapterConfiguration config     = new AdapterConfiguration(this);
		nePasswordMaskCharacters        = config.getStringValue("ne.passwordMaskCharacters", "******");
		neLineTermination               = config.getNeLineTermination();
		cliWaitForPromptInMilliseconds  = config.getIntegerValue("interfaceType.cli.waitForPromptInMilliseconds", 5000);
		cliCommandTimeoutInMilliseconds = config.getIntegerValue("interfaceType.cli.defaultTimeoutInMilliseconds", 60000);
		cliLoginPrompt                  = config.getStringValue("interfaceType.cli.loginPrompt", "login:");
		cliPasswordPrompt               = config.getStringValue("interfaceType.cli.passwordPrompt", "Password:");
		cliInitialPromptString          = config.getStringValue("interfaceType.cli.initialNEPrompt", "\\$$|\\$ $|#$|# $");
		sshSimpleCommand                = config.getStringValue("interfaceType.cli.sshSimpleCommand", "pwd");
		standardPrompt                  = config.getStringValue("interfaceType.cli.standardPrompt", "NakinaPrompt>>");
		supportsStandardPrompt          = config.getBooleanValue("interfaceType.cli.supportsStandardPrompt", false);

		boolean loginCalled = false;

		if (managementProtocol.equals(ManagementProtocol.CLI)) {
			if (connectionProtocol.equals(ConnectionProtocol.SSH)) {
				loginCalled = true;
				doCLISSHLogin(credentials);
				
			} else if (connectionProtocol.equals(ConnectionProtocol.TELNET)) {
				loginCalled = true;
				doTelnetLogin(credentials.getUserName(), credentials.getPassword());
			}
		}

		if (!loginCalled) {
			throw new RequestFailure("Login request failed: management protocol " + managementProtocol.toString()
					+ ", connection protocol: " + connectionProtocol.toString() + " is not supported\n");
		}

		traceInfo(getClass(), "Login successful.");
		return getAuthenticationResponseData();
	}

	protected void doTelnetLogin(String username, String password) throws RequestFailure {
		String charsBeforeLogin = null;
		try {
			charsBeforeLogin = waitForPrompt(cliLoginPrompt, cliWaitForPromptInMilliseconds);
		} catch (RequestFailure e) {
			traceWarn(getClass(), "RequestFailure execption caught on waitForPrompt. Possible race-condition occured.");
		}

		CLICommand useridCommand = new CLICommand(this, "loginPromptResponse", username + neLineTermination,
				cliPasswordPrompt, cliCommandTimeoutInMilliseconds);
		useridCommand.send();
		String charsAfterLoginPrompt = useridCommand.removeEchoAndPrompt();

		// NE has output it's password prompt - our response will be the
		// password and a carriage return. We will expect a
		// prompt to indicate login successful

		CLICommand pwdCommand = new CLICommand(this, "passwordPromptResponse", password + neLineTermination,
				nePasswordMaskCharacters, cliInitialPromptString, cliCommandTimeoutInMilliseconds);
		String charsAfterPasswordPrompt = pwdCommand.send();

		// NOTE: if the username or password are incorrect, the pwdCommand will
		// time-out, and return a RequestFailure
		// exception.

		setAuthenticationResponseData(charsBeforeLogin, charsAfterLoginPrompt, charsAfterPasswordPrompt);
	}

	protected void doCLISSHLogin(AuthenticationArguments credentials) throws RequestFailure {

		String charsBeforeLoginPrompt = null;
		try {
			charsBeforeLoginPrompt = waitForPrompt(cliInitialPromptString, cliWaitForPromptInMilliseconds);
		} catch (RequestFailure e) {
			traceWarn(getClass(), "RequestFailure execption caught on waitForPrompt. Possible race-condition occured.");
		}
		String charsAfterLoginPrompt = null;
		
		traceInfo(getClass(), "waitForPrompt result: " + charsBeforeLoginPrompt);
		
		// Send a simple command just to check that the NE is responding.
		CLICommand simpleCommand = new CLICommand(this, "Just checking", sshSimpleCommand + neLineTermination,
				cliInitialPromptString, cliCommandTimeoutInMilliseconds);
		simpleCommand.send();
		
		if(isGateway == false) {
			
			//If we're subtending, do the second hop login 			
			String               userID                    = credentials.getUserName();
	        String               password                  = credentials.getPassword();
	        CLICommandManager    cmdManager                = getCLICommandManager();	        
	        SessionAddressHolder sessionAddressHolder      = cmdManager.getSessionAddressHolder();
	        Map<String,String>   sessionAddressInformation = sessionAddressHolder.getSessionAddresses();
	        String               port                      = sessionAddressInformation.get("Port");
	        String               ipAddress                 = sessionAddressInformation.get("IP Address");
	        
	        traceInfo(getClass(), logPrefix+"Sending command: ssh -p " + port + " " + userID + "@" + ipAddress + "; kill -SIGHUP $PPID;");
	        String sshResponse = new CLICommand(this, "Second Hop", "ssh -p " + port + " " + userID + "@" + ipAddress + "; kill -SIGHUP $PPID;\n", 
	        		"Performing Second Hop", "assword|authenticity").send();

	        traceInfo(getClass(), logPrefix+"SSHResponse=>"+sshResponse);
	        
	        if (sshResponse.contains("authenticity")){
	            CLICommand acceptKey = new CLICommand(this, "Accept SSH Key", "yes\n", "Accepting SSH Key", "assword");
	            acceptKey.send();
	            traceInfo(getClass(), logPrefix+"Accepted SSH Key");
	        }

	        traceInfo(getClass(), logPrefix+"Sending the password =>"+password);
	        String passwordResponse = new CLICommand(this, "Sending Password", password + "\n", "\\$|#|>|assword:").send();
	        traceInfo(getClass(), logPrefix+"PWResponse=>"+passwordResponse);
	       
		}
		
		if(supportsStandardPrompt) setStandardPrompt(standardPrompt);

		setAuthenticationResponseData(charsBeforeLoginPrompt, charsAfterLoginPrompt, null);
	}
	
	public void setStandardPrompt(String prompt) throws RequestFailure {
		Executor executor   = new Executor(this);
		String getPromptCmd = "";
		String cmd          = "PS1=\""+prompt+"\"";
		
		ExecutionResult result = executor.execute(Command.create(getPromptCmd));
		originalPrompt=result.getOutput();
		originalPrompt = originalPrompt.replaceAll("\n", "");
		traceInfo(this.getClass(), "OriginalPrompt: " + originalPrompt);
		
		result = executor.execute(Command.create(cmd).retrieveExitCode());
	}
	
	public void resetStandardPrompt() throws RequestFailure {
		Executor executor   = new Executor(this);
		String cmd          = "PS1=\""+originalPrompt+"\"";
				
		ExecutionResult result = executor.execute(Command.create(cmd).retrieveExitCode());
	}

	protected String waitForPrompt(String prompt, int timeout) throws RequestFailure {
		traceInfo(this.getClass(), "Waiting for prompt: " + prompt + ", timeout: " + timeout);
		WaitForStringCommand waitCommand = new WaitForStringCommand(this);
		String charactersFound = waitCommand.waitForAutonomousString(prompt, timeout);
		traceInfo(this.getClass(), "Data received: " + charactersFound);
		return charactersFound;
	}

	/**
	 * This method sets the banner value for this login. The three input
	 * parameters represent the characters received before the login, the
	 * characters received between sending the userid and password, and the
	 * characters received after sending the password. If there are no
	 * characters (or no need for the characters) in any one of the three
	 * strings, then that value will not be added to the banner.
	 * 
	 * If the NE's banner does not behave in the manner implemented in this
	 * method, then override this method in the LoginRequestHandler in the model
	 * package.
	 * 
	 * By default, the AuthenticationResponseData returned from this method
	 * represents a successful login. If the login is not successful, the method
	 * AuthenticationResponseData.setAuthenticationSuccessToFalse(String
	 * authenticationerror) can be used or this response data can be ignored.
	 * 
	 * @param charsBeforeLogin
	 * @param charsAfterLoginPrompt
	 * @param charsAfterPasswordPrompt
	 * @return
	 */
	protected void setAuthenticationResponseData(String charsBeforeLogin, String charsAfterLoginPrompt,
			String charsAfterPasswordPrompt) {
		authenticationResponseData = getAuthenticationResponseData();

		String banner = "";
		if (charsBeforeLogin != null && !charsBeforeLogin.equals("")) {
			banner = charsBeforeLogin;
		}
		if (charsAfterLoginPrompt != null && !charsAfterLoginPrompt.equals("")) {
			banner = banner + charsAfterLoginPrompt;
		}
		if (charsAfterPasswordPrompt != null && !charsAfterPasswordPrompt.equals("")) {
			banner = banner + charsAfterPasswordPrompt;
		}

		if (!banner.equals("")) {
			authenticationResponseData.setBanner(banner);
		}
	}

	private AuthenticationResponseData getAuthenticationResponseData() {
		if (authenticationResponseData == null) {
			authenticationResponseData = new AuthenticationResponseData();
		}
		return authenticationResponseData;
	}
}
