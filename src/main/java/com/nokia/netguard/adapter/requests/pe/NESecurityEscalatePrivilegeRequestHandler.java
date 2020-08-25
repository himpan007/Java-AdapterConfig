package com.nokia.netguard.adapter.requests.pe;
        
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.nesecurity.EscalatePrivilegeBase;
import com.nokia.netguard.adapter.capabilities.NEInterfaceCapabilityHandler;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.callback.VerifyOutput;

/** 
 * This feature is used to manage the three escalate privilege features which allow
 * a user to escalate their privilege, escalate their privilege for the duration of
 * a single command and to switch their current user.
 */
public class NESecurityEscalatePrivilegeRequestHandler extends EscalatePrivilegeBase {

    protected String  neLineTermination = null;
    protected Integer waitForPromptInMilliseconds = null;
    protected Integer cliCommandTimeoutInMilliseconds = null;
    protected String  cliPromptPattern = null;
    protected String  passwordMask = null;

    /**
     * Escalates the privilege level (i.e. Cisco "enable" command).
     * 
     * @param password
     *            the current user's password to use. It may not be null or empty.
     * @throws RequestFailure
     *             if any errors occur during the processing of this request.
     */
    public void escalatePrivilege(String password) throws RequestFailure {
    	Executor executor = new Executor(this);
    	
    	executor.execute(Command.create("su").addPrompt("Password:( ?)$"));
    	executor.execute(Command.create(password).addCallback(VerifyOutput.create().addErroneousOutput("Authentication failure").addErroneousOutput("No passwd entry for user")));
    }

    /**
     * Escalates the privilege level for the duration of a command (i.e. Linux "sudo" command).
     * 
     * @param password
     *            the current user's password to use. It may not be null or empty.
     * @param command
     *            the command to execute once the privilege level is escalated. It may not be null or empty.
     * @return the command's execution output
     * @throws RequestFailure
     *             if any errors occur during the processing of this request.
     */
    public String escalatePrivilegeForTheDurationOfACommand(String password, String command)
            throws RequestFailure {
        final StringBuilder response = new StringBuilder();
        Executor executor = new Executor(this);
        executor.execute(Command.create(command).escalateCommand(getAuthenticationArguments(getRequestInterfaceName()).getPassword())
                .addCallback(executionResult -> response.append(executionResult.getOutput())));
        
        return response.toString();
    }

    /**
     * Switches user (i.e. Linux "su" command).
     * 
     * @param userId
     *            the user id of the user we are switching to. It may not be null or empty.
     * @param password
     *            the password of the user we are switching to. It may not be null or empty.
     * @throws RequestFailure
     *             if any errors occur during the processing of this request.
     */
    public void switchUser(String userId, String password) throws RequestFailure {
    	Executor executor = new Executor(this);
    	
    	executor.execute(Command
    			.create("su - " + userId)
    			.addPrompt("Password:( ?)$")
    			.addCallback(VerifyOutput
    					.create()
    					.addErroneousOutput("No passwd entry for user")
    					.addErroneousOutput("does not exist")));
    	
    	executor.execute(Command
    			.create(password)
    			.setMasked()
    			.addCallback(VerifyOutput
    					.create()
    					.addErroneousOutput("Authentication failure")));
    }
}