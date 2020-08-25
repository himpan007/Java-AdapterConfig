package com.nokia.netguard.adapter.capabilities;

import java.util.Arrays;

import com.nakina.adapter.api.capabilities.nemanager.NEInterfaceCommunicationsProfile;
import com.nakina.adapter.api.capabilitiesbuilder.ne.NEInterfaceCapabilityBuilder;
import com.nakina.adapter.api.capabilitiesbuilder.ne.NEInterfaceListCapabilityBuilder;
import com.nakina.adapter.api.capabilitiesbuilder.ne.NEInterfaceSessionAddressBuilder;
import com.nakina.adapter.api.capabilitiesbuilder.ne.NEInterfaceSessionAddressListBuilder;
import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.capability.api.NEInterfaceCapabilityBase;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nakina.oss.shared.type.NESecurityCredentialType;
import com.nokia.netguard.adapter.configuration.Constants;

public class NEInterfaceCapabilityHandler extends NEInterfaceCapabilityBase {        

	public static final String CLI                 = "CLI";
	protected static final String SFTP                = "SFTP";
	private   static final String SSH_PROFILE         = "CLI_SSH";
	private   static final String TELNET_PROFILE      = "CLI_TELNET";
	private   static final String SSH_DEFAULT_PORT    = "22";
	private   static final String TELNET_DEFAULT_PORT = "23";
    private   static final int    CLI_MAX_SESSIONS    = 10;
    
    private boolean isGateway = Constants.IS_GATEWAY;
    private String  logPrefix = Constants.LOG_PREFIX;
        

    @Override
    public NEInterfaceListCapabilityBuilder getCapability() {
    	
    	Trace.info(this.getClass(), logPrefix+"This adapter is a gateway = "+isGateway);    	
    	
        NEInterfaceListCapabilityBuilder neInterfaceListCapability = new NEInterfaceListCapabilityBuilder();
        
        Object[][] interfaceData = Constants.INTERFACE_DEFINITIONS;        
        for (int loop = 0; loop < interfaceData.length; loop++) {
        	Trace.info(this.getClass(), logPrefix+"Creating interface:"+(String)interfaceData[loop][0]);
        	neInterfaceListCapability.add(createCliInterface((String)interfaceData[loop][0], (ConnectionProtocol)interfaceData[loop][1], (boolean)interfaceData[loop][2]));
        }
        
        return neInterfaceListCapability;
    }
    
    private NEInterfaceCapabilityBuilder createCliInterface(String interfaceName, ConnectionProtocol connectionProtocol, boolean managementSupport) {  
    	
        NEInterfaceCapabilityBuilder cliInterface = new NEInterfaceCapabilityBuilder(interfaceName, ManagementProtocol.CLI, NESecurityCredentialType.ACCOUNT); 
        
        cliInterface.setMaxSessions(CLI_MAX_SESSIONS);
        cliInterface.setTerminalWidthInCharacters(Integer.MAX_VALUE);
        cliInterface.setManagementSupport(true);
        
        if(isGateway) {
        	cliInterface.setSessionTunnelProxy(true);
        	
        }else{
            cliInterface.setRequiresGatewayAuthentication(true);     
            NEInterfaceSessionAddressListBuilder list          = new NEInterfaceSessionAddressListBuilder();        
            NEInterfaceSessionAddressBuilder     secondHopIP   = new NEInterfaceSessionAddressBuilder("IP Address","IP");        
            NEInterfaceSessionAddressBuilder     secondHopPort = new NEInterfaceSessionAddressBuilder("Port","Port");        
            list.add(secondHopIP);        
            list.add(secondHopPort);        
            cliInterface.setSessionAddresses(list); 
        }
        
    	NEInterfaceCommunicationsProfile sshProfile    = new NEInterfaceCommunicationsProfile(SSH_PROFILE, SSH_DEFAULT_PORT, ConnectionProtocol.SSH);
    	NEInterfaceCommunicationsProfile telnetProfile = new NEInterfaceCommunicationsProfile(TELNET_PROFILE,TELNET_DEFAULT_PORT,ConnectionProtocol.TELNET);
    	cliInterface.setCommunicationsProfiles(Arrays.asList(sshProfile, telnetProfile));
        
        String defaultPort = SSH_DEFAULT_PORT;
        if (connectionProtocol.equals(ConnectionProtocol.TELNET))
        	defaultPort = TELNET_DEFAULT_PORT;
        cliInterface.setCommunicationsProfile(SSH_PROFILE, defaultPort, connectionProtocol);
        return cliInterface;
    }   
     
}
