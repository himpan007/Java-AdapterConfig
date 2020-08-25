package com.nokia.netguard.adapter.requests.gs.resource.retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.nakina.adapter.api.responsedatabuilder.ResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.ResourceBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.AccountCredentialBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.CredentialSecuritySettingsBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.CredentialsResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.nesecurity.resource.LocalCredentialStoreResponseDataBuilder;
import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.api.type.inventory.common.ResourceMinimalId;
import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.configuration.Constants;

public class RetrieveUsersRawAttributes {
	public static final String FILTER_OUT_USERS_PROPERTY   = "getUsers.filterOutDefaultUsers";
	public static final String GET_SYSTEM_USERS_PROPERTY   = "getUsers.getSystemUsers";
	public static final String DEFAULT_USERS_LIST_PROPERTY = "getUsers.defaultUsersList.user";


	private static final String LSLOGINS_OUTPUT  = "USER,UID,GECOS,HOMEDIR,GROUP,GID,SUPP-GROUPS,SUPP-GIDS,LAST-LOGIN,PWD-WARN,PWD-CHANGE,PWD-MIN,PWD-MAX,PWD-EXPIR,PROC";

	private static final String ATTR_UID         = "User ID";
	private static final String ATTR_GECOS       = "Full user name";
	private static final String ATTR_HOMEDIR     = "Home directory";
	private static final String ATTR_GROUP       = "Primary group name";
	private static final String ATTR_GID         = "Primary group ID";
	private static final String ATTR_SUPP_GROUPS = "Supplementary group names";
	private static final String ATTR_SUPP_GIDS   = "Supplementary group IDs";
	private static final String ATTR_LAST_LOGIN  = "Date of last login";
	private static final String ATTR_PWD_WARN    = "Days user is warned of password expiration";
	private static final String ATTR_PWD_CHANGE  = "Date of last password change";
	private static final String ATTR_PWD_MIN     = "Number of days required between changes";
	private static final String ATTR_PWD_MAX     = "Max number of days a password may remain unchanged";
	private static final String ATTR_PWD_EXPIR   = "Password expiration date";

	private RetrieveUsersRawAttributes() {
	}

	public static List<ResponseDataBuilder> getAccountsRawAttributes(BaseCommand base, Configuration config) throws RequestFailure{
		
		List<ResponseDataBuilder> rawResponseDataList = new ArrayList<>();

		Executor          executor    = new Executor(base);
		ResourceBuilder   rootElement = new ResourceBuilder(Constants.ROOT_MANAGED_ELEMENT, Constants.ROOT_CONFIGURATION, Constants.ROOT_RESOURCE_TYPE);
		Trace.info(RetrieveUsersRawAttributes.class, Constants.LOG_PREFIX + "Creating the base builder with "+Constants.ROOT_MANAGED_ELEMENT
				+" "+Constants.ROOT_CONFIGURATION+" "+Constants.ROOT_RESOURCE_TYPE);

		ResourceMinimalId parentID    = new ResourceMinimalId(rootElement.getEntityId(), rootElement.getEntityType(), rootElement.getResourceType());
		
		CredentialsResponseDataBuilder          credStores    = new CredentialsResponseDataBuilder(parentID);		
		LocalCredentialStoreResponseDataBuilder accountsStore = new LocalCredentialStoreResponseDataBuilder((String)Constants.CREDENTIAL_DB_DEFINITIONS[0][0]);

		boolean      addDefaultUsers  = config.getBoolean(FILTER_OUT_USERS_PROPERTY, false);
		boolean      getSystemUsers   = config.getBoolean(GET_SYSTEM_USERS_PROPERTY, false);
		List<Object> defaultUsersList = config.getList(DEFAULT_USERS_LIST_PROPERTY, Collections.emptyList());
		Set<Object>  defaultUsers     = new HashSet<>(defaultUsersList);

		StringBuilder command = new StringBuilder("lslogins --time-format=iso --noheadings -c -u");
		if(getSystemUsers) {
			command.append(" -s");
		}
		command.append(" -o=");
		command.append(LSLOGINS_OUTPUT);

		Command cmd = Command.create(command.toString()).escalateCommand();

		String   cmdResponse  = executor.execute(cmd).getOutput();
		String[] lsloginsInfo = cmdResponse.split("\r?\n");

		for (String infoEntry : lsloginsInfo) {
			
			String[] infoArray =  infoEntry.split(":");
			if (infoArray.length < 14 || infoArray[0].isEmpty() || (!addDefaultUsers && defaultUsers.contains(infoArray[0]))) {
				continue;
			}

			AccountCredentialBuilder          userResource     = new AccountCredentialBuilder(infoArray[0]);	
			CredentialSecuritySettingsBuilder securitySettings = new CredentialSecuritySettingsBuilder(infoArray[0]);
			
			if (addDefaultUsers && defaultUsers.contains(infoArray[0])) {
				userResource.setIsDefaultCredential(Boolean.TRUE);
			}

			userResource.addRawAttr(ATTR_UID,         infoArray[1]);
			userResource.addRawAttr(ATTR_GECOS,       infoArray[2]);
			userResource.addRawAttr(ATTR_HOMEDIR,     infoArray[3]);
			userResource.addRawAttr(ATTR_GROUP,       infoArray[4]);
			userResource.addRawAttr(ATTR_GID,         infoArray[5]);
			userResource.addRawAttr(ATTR_SUPP_GROUPS, infoArray[6]);

			securitySettings.addRawAttr(Constants.SECURITY_PROFILE_CUSTOM_GROUPS, infoArray[6]);
			userResource.addSecuritySettings(securitySettings);

			userResource.addRawAttr(ATTR_SUPP_GIDS,  infoArray[7]);
			userResource.addRawAttr(ATTR_LAST_LOGIN, infoArray[8]);
			userResource.addRawAttr(ATTR_PWD_WARN,   infoArray[9]);
			userResource.addRawAttr(ATTR_PWD_CHANGE, infoArray[10]);
			userResource.addRawAttr(ATTR_PWD_MIN,    infoArray[11]);
			userResource.addRawAttr(ATTR_PWD_MAX,    infoArray[12]);
			userResource.addRawAttr(ATTR_PWD_EXPIR,  infoArray[13]);

			Trace.info(RetrieveUsersRawAttributes.class, Constants.LOG_PREFIX + "Adding user "+userResource.getEntityId());
			accountsStore.addCredential(userResource);
		}

		Trace.info(RetrieveUsersRawAttributes.class, Constants.LOG_PREFIX + "Adding account "+accountsStore.getEntityId());
		credStores.addCredentialStore(accountsStore);
		rawResponseDataList.add(credStores);		

		return rawResponseDataList;

	}


}
