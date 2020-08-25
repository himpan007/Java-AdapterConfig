package com.nokia.netguard.adapter.configuration;

import com.nakina.oss.shared.type.ConnectionProtocol;

public class Constants {
	public static final String LOG_PREFIX = "[Jinny-KVM_centos_CLI-V1] ";

	//Two-hop adapter type setting. Set false for subtending.
	public static final boolean  IS_GATEWAY                     = true;
	
	//User mapping
	public static final String   SECURITY_PROFILE_GROUPS        = "groups";
	public static final String   SECURITY_PROFILE_CUSTOM_GROUPS = "customGroups";
	
	public static final String   ROOT_MANAGED_ELEMENT           = "ManagedElement";
	public static final String   ROOT_CONFIGURATION             = "Configuration";
	public static final String   ROOT_RESOURCE_TYPE             = "ManagedElement";
    
	//{<InterfaceName>,<ConnectionProtocol>,<ManagementSupport>}
    public static final Object[][] INTERFACE_DEFINITIONS = {
    		{"CLI", ConnectionProtocol.SSH, true},
    		{"SFTP", ConnectionProtocol.SSH, false},
    		{"AM", ConnectionProtocol.ADAPTER_MANAGED, true}
    };
    
    //{<UserCredentialDbName><InterfaceName-1>..<InterfaceName-n>}
    public static final String[][] CREDENTIAL_DB_DEFINITIONS = {
    		{"Accounts", "CLI", "SFTP"},
    		{"Super", "AM"}
    };
	private Constants() {}
}
