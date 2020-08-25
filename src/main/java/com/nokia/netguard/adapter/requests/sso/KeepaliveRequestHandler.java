package com.nokia.netguard.adapter.requests.sso;

import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.KeepaliveStrategyBase;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.cli.callback.CheckExitCode;

/**
 * This class handles the Keepalive_REQ for all interfaces.
 * The Keepalive_REQ is used to maintain a session with the system. If the Keepalive_REQ fails, it is assumed
 * that the interface has gone down to the NE.
 * <p>
 */
public class KeepaliveRequestHandler extends KeepaliveStrategyBase {

    protected String  cliKeepaliveCommand = null;

    public void doKeepalive(ManagementProtocol managementProtocol, ConnectionProtocol connectionProtocol)
    		throws RequestFailure {
    	Executor executor = new Executor(this);

        AdapterConfiguration config = new AdapterConfiguration(this);
        cliKeepaliveCommand = config.getStringValue("interfaceType.cli.keepaliveCommand", "pwd");

        if (managementProtocol.equals(ManagementProtocol.CLI)) {
        	executor.execute(Command.create(cliKeepaliveCommand).retrieveExitCode().addCallback(CheckExitCode.create()));
            return;
        }

        throw new RequestFailure("The Keepalive request failed: the management protocol is not supported: "
                    + managementProtocol.toString());
    }

}
