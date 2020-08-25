
//NAKINA GENERATED HEADER - START
//DO NOT EDIT THE HEADER
//Copyright (c) 2016-2020. Nokia Solutions and Networks Oy. All rights  reserved.
//The modules included herein are subject to a restricted use license and can only  be used in conjunction with this application.
//VERSION: NokiaADK-DEV-18.2.0.15
//DATE: Tue Jun 26 14:22:52 CEST 2018
//CHECKSUM: 2115190356
//NAKINA GENERATED HEADER - FINISH

package com.nokia.netguard.adapter.requests.sso;

import com.nakina.adapter.api.requestarguments.sessionmanagement.AuthenticationArguments;
import com.nakina.adapter.api.responsedata.sessionmanagement.AuthenticationResponseData;
import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.LogoutStrategyBase;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;

 /*
 * Do not edit. Instead create a subclass in the model directory, and override methods as needed.
 * <p>
 * This class logs the user out of the NE.
 *
 */
public class LogoutRequestHandler extends LogoutStrategyBase {

    protected String cliLogoutCommand = null;
    protected String cliLogoutCompleteRegex = null;
    protected String cliSSHLogoutCompleteRegex = null;

    @Override
    public AuthenticationResponseData doLogout(AuthenticationArguments credentials, ManagementProtocol managementProtocol,
            ConnectionProtocol connectionProtocol) throws RequestFailure {
        traceInfo(getClass(), "Logout Request for Interface: " + managementProtocol.toString() + " Connection Protocol: "
                + connectionProtocol.toString());

        AdapterConfiguration config = new AdapterConfiguration(this);
        cliLogoutCommand = config.getStringValue("interfaceType.cli.logoutCommand", "exit");
        cliLogoutCompleteRegex = config.getStringValue("interfaceType.cli.logoutRegex", "logout");
        cliSSHLogoutCompleteRegex = config.getStringValue("interfaceType.cli.sshLogoutCompleteRegex", ".*");

        boolean logoutCalled = false;

        if (managementProtocol.equals(ManagementProtocol.CLI)) {
            if (connectionProtocol.equals(ConnectionProtocol.SSH)) {
                logoutCalled = true;
                doCLISSHLogout();
            } else {
                logoutCalled = true;
                doCLILogout();
            }
        }

        // NOTE: Logout must always succeed.  If the management protocol was not recognized,
        //       then assume that no action is required.

        if (!logoutCalled) {
            traceInfo(getClass(), "Logout request: management protocol " + managementProtocol.toString()
                    + ", connection protocol: " + connectionProtocol.toString() + " is not supported\n");
        }

        traceInfo(getClass(), "Logout successful.");
        return new AuthenticationResponseData();
    }
    /**
     * If a CLI interface is being used for logout, then this method will send LOGOUT to the NE and expect
     * a string containing either "Complete" or "complete" to indicate the logout was completed.
     *
     * If your NE does a different sequence, then override this method and handle the sequence of prompts it displays.
     * @throws RequestFailure
     */
    protected void doCLILogout() throws RequestFailure {
    	Executor executor = new Executor(this);
    	executor.execute(Command.create(cliLogoutCommand).addPrompt(cliLogoutCompleteRegex));
    }

    /**
     * If your NE supports a CLI SSH login, then this method will handle logout.
     * Basically it will be the same as the regular CLI logout but with out a response message
     * The assumption is that the logout command is "LOGOUT"
     * @throws RequestFailure
     */
    protected void doCLISSHLogout() throws RequestFailure {
    	Executor executor = new Executor(this);
    	executor.execute(Command.create(cliLogoutCommand).addPrompt(cliSSHLogoutCompleteRegex));
    }
}