package com.nokia.netguard.adapter.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nakina.adapter.api.capabilities.nesecurity.AccountAttributesCapability;
import com.nakina.adapter.api.capabilities.nesecurity.AccountDBCapability;
import com.nakina.adapter.api.capabilities.nesecurity.CredentialAttributeRulesCapability;
import com.nakina.adapter.api.capabilities.nesecurity.CredentialDBCapability;
import com.nakina.adapter.api.capabilities.nesecurity.NESecurityCapability;
import com.nakina.adapter.api.capabilities.nesecurity.SecurityProfileAdapterSetting;
import com.nakina.adapter.api.capabilities.nesecurity.SecurityProfileSettingBase;
import com.nakina.adapter.api.capabilities.nesecurity.SecurityProfileSettingsCapability;
import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.capability.api.NESecurityCapabilityBase;
import com.nokia.netguard.adapter.configuration.Constants;

public class NESecurityCommonCapabilityStrategy extends NESecurityCapabilityBase {

	
	public    static final String   CREDENTIAL_DB_NAME = "Accounts";
	protected static final String[] PROFILE_GROUPS     = new String[] {"adm","sys","users","wheel"};
	
    private String  logPrefix = Constants.LOG_PREFIX;
	
	
	@Override
	public NESecurityCapability getCapability() {
		
		ArrayList<CredentialDBCapability> dblist = new ArrayList<>();
        String[][] databaseDefinitions = Constants.CREDENTIAL_DB_DEFINITIONS;        
    	Trace.info(this.getClass(), logPrefix+"There are "+databaseDefinitions.length+" user credential databases.");
    	
    	int numberOfInterfaces = 0;
        for (int loop = 0; loop < databaseDefinitions.length; loop++) {
        	
        	numberOfInterfaces = databaseDefinitions[loop].length -1;
        	Trace.info(this.getClass(), logPrefix+"Creating database "+(String)databaseDefinitions[loop][0]+" on "+numberOfInterfaces+" interfaces");
        	
        	List<String> interfaceList = new ArrayList<String>();
        	for(int interfaceLoop = 1; interfaceLoop < numberOfInterfaces+1; interfaceLoop++) {
        		String interfaceName = databaseDefinitions[loop][interfaceLoop];
            	Trace.info(this.getClass(), logPrefix+"Adding interface "+interfaceName);
        		interfaceList.add(interfaceName);
        	}  
        	String[] interfaceArray = new String[interfaceList.size()];
    		addLocalCredentialDB(dblist, databaseDefinitions[loop][0], interfaceList.toArray(interfaceArray));
        }
        
		return new NESecurityCapability(dblist);
	}

	protected void addLocalCredentialDB(ArrayList<CredentialDBCapability> dblist, String credentialDBName, String[] neIntefaceNames) {
		
		List<String> interfaceNames;
		interfaceNames                                   = Arrays.asList(neIntefaceNames);
		AccountDBCapability accountDBCapability          = new AccountDBCapability(false, credentialDBName, true, neIntefaceNames[0], interfaceNames);		
        AccountAttributesCapability attributesCapability = new AccountAttributesCapability();
        List<String> groups                              = new ArrayList<>(); 

		groups.addAll(Arrays.asList(PROFILE_GROUPS));
	    
        CredentialAttributeRulesCapability passwordRules = new CredentialAttributeRulesCapability();
        CredentialAttributeRulesCapability usernameRules = new CredentialAttributeRulesCapability();   
        
        attributesCapability.setUsernameRules(usernameRules);
        attributesCapability.setPasswordRules(passwordRules);

        attributesCapability.setSupportsVendorUAPMultiselect(true);
        attributesCapability.setSupportsVendorUAPCustomization(true);
        accountDBCapability.setAccountAttributesCapability(attributesCapability);
        addSecurityProfileInstance(accountDBCapability, groups);
		dblist.add(accountDBCapability);
	}
	
	private void addSecurityProfileInstance(AccountDBCapability accountDB, List<String> groups){
		SecurityProfileSettingsCapability securitySettings = new SecurityProfileSettingsCapability();
		
		SecurityProfileAdapterSetting groupsList = SecurityProfileAdapterSetting.createSetting(Constants.SECURITY_PROFILE_GROUPS, 
				"User groups", SecurityProfileSettingBase.SettingTypes.STRING_LIST, groups, null, null, Boolean.TRUE,
				"Linux user groups", 
				Boolean.FALSE);
		securitySettings.addSecurityProfileSetting(groupsList);
		
		SecurityProfileAdapterSetting customGroupsString = SecurityProfileAdapterSetting.createSetting(Constants.SECURITY_PROFILE_CUSTOM_GROUPS, 
				"User custom groups", SecurityProfileSettingBase.SettingTypes.STRING, "", null, null, Boolean.TRUE,
				"Comma separated list of user groups", 
				Boolean.FALSE);
		securitySettings.addSecurityProfileSetting(customGroupsString);
		
        SecurityProfileAdapterSetting maxdays = SecurityProfileAdapterSetting.createSetting("maxdays", "Maximum Days Between Password Change",        						
                SecurityProfileSettingBase.SettingTypes.INTEGER, 
                -1, null, null, null,
                "The maximum number of days during which a password is valid.  A value of -1 will remove checking a password's validity.",
                false);
        securitySettings.addSecurityProfileSetting(maxdays);

		accountDB.setSecurityProfileSettingsCapability(securitySettings);
    }
	
/** Examples of other data structures that can be used in security templates
 
        Instant now = Instant.now();
        SecurityProfileAdapterSetting expiredate = SecurityProfileAdapterSetting.createSetting("expiredate", "Password Expiry Date",        						
				                                                                   SecurityProfileSettingBase.SettingTypes.DATE, 
                                                                                   now, null, null, null,
                                                                                   "The date on which the user's account will no longer be accessible.",
                                                                                   false);
        securitySettings.addSecurityProfileSetting(expiredate);
        
        SecurityProfileAdapterSetting inactivityLock = SecurityProfileAdapterSetting.createSetting("inactive", "Account Inactivity Days",        						
		                                                                           SecurityProfileSettingBase.SettingTypes.INTEGER, 
                                                                                   -1, null, null, null,
                                                                                   "The number of days of inactivity after a password has expired before the account is locked. A value of -1 will remove an account's inactivity.",
                                                                                   false);
        securitySettings.addSecurityProfileSetting(inactivityLock);
        
        SecurityProfileAdapterSetting mindays = SecurityProfileAdapterSetting.createSetting("mindays", "Minimum Days Between Password Change",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.INTEGER, 
                                                                                   0, null, null, null,
                                                                                   "The minimum number of days between password changes.  A value of zero for this field indicates that the user may change his/her password at any time.",
                                                                                   false);
        securitySettings.addSecurityProfileSetting(mindays);
        
        SecurityProfileAdapterSetting maxdays = SecurityProfileAdapterSetting.createSetting("maxdays", "Maximum Days Between Password Change",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.INTEGER, 
                                                                                   -1, null, null, null,
                                                                                   "The maximum number of days during which a password is valid.  A value of -1 will remove checking a password's validity.",
                                                                                   false);
        securitySettings.addSecurityProfileSetting(maxdays);
        
        SecurityProfileAdapterSetting warndays = SecurityProfileAdapterSetting.createSetting("warndays", "Days Before Password Change For Warning",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.INTEGER, 
                                                                                   0, null, null, null,
                                                                                   "The number of days of warning before a password change is required.",
                                                                                   false);
        securitySettings.addSecurityProfileSetting(maxdays);
        
        SecurityProfileAdapterSetting sudostring = SecurityProfileAdapterSetting.createSetting("sudostring", "Sudo String",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.STRING, 
                                                                                   "", null, null, null, null, false);
        securitySettings.addSecurityProfileSetting(sudostring);
                
        SecurityProfileAdapterSetting fakeboolean = SecurityProfileAdapterSetting.createSetting("fake boolean", "Test Fake Boolean",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.BOOLEAN, 
                                                                                   false, null, null, null, null, true);
        securitySettings.addSecurityProfileSetting(fakeboolean);

        SecurityProfileAdapterSetting fakefloat = SecurityProfileAdapterSetting.createSetting("fakefloat", "Test Float",        						
                                                                                   SecurityProfileSettingBase.SettingTypes.FLOAT, 
                                                                                   10f, 1f, 50f, null, null, true);
        securitySettings.addSecurityProfileSetting(fakefloat);
        
        SecurityProfileAdapterSetting fakemultiselect = SecurityProfileAdapterSetting.createSetting("fakemultiselect", "Test Multi Select List",        						
                                                                                SecurityProfileSettingBase.SettingTypes.STRING_LIST, 
                                                                                Arrays.asList("Access Live Settings",
                                                                                              "Approve Resource Access",
                                                                                              "Manage Resource Access",
                                                                                              "Manage Other Sessions",
                                                                                              "Manage Credentials",
                                                                                              "Manage All Sessions",
                                                                                              "View Resource Access",
                                                                                              "View Other Sessions",
                                                                                              "View Credentials",
                                                                                              "View Passwords In Plain Text",
                                                                                              "View All Sessions"),
                                                                                null, null, true, null, true);
        securitySettings.addSecurityProfileSetting(fakemultiselect);
        
        SecurityProfileAdapterSetting fakesingleselect = SecurityProfileAdapterSetting.createSetting("fakesingleselect", "Test Single Select List",        						
                                                                                SecurityProfileSettingBase.SettingTypes.STRING_LIST, 
                                                                                Arrays.asList("admin", "reader", "writer"), null, null, false, null, true);
        securitySettings.addSecurityProfileSetting(fakesingleselect);
        
**/

}
