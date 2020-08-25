package com.nokia.netguard.adapter.requests.gs.reconcile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.BaseCommand;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.requests.gs.data.collecting.CSVReader;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.AttributeRulesCSVStruct;
import com.nokia.netguard.adapter.requests.gs.reconcile.attribute.ReconcileAttributeBuilder;
import com.nokia.netguard.adapter.requests.gs.reconcile.attribute.ReconcileCommandData;
import com.nokia.netguard.adapter.requests.gs.reconcile.resource.ResourceRuleCommands;

public class ReconcileCommon {
	public static final String CUSTOMER_CONFIGURATION_RAW_ATTRIBUTE_RECONCILE_FILENAME_PROPERTY = "interfaceType.cli.resourceRawDataConfigFile.rawAttributeReconcileRule";
	public static final String CUSTOMER_CONFIGURATION_RESOURCE_RECONCILE_FILENAME_PROPERTY = "interfaceType.cli.resourceRawDataConfigFile.resourceReconcileRule";
	public static final String CUSTOMER_CONFIGURATION_LOCATION_PROPERTY = "interfaceType.cli.resourceRawDataConfigFile.location";
	private BaseCommand base;
	
	public static final String COMMAND_KEYWORD_ATTRNAME = "$ATTRNAME";
	public static final String COMMAND_KEYWORD_ATTRVALUE = "$ATTRVALUE";
	public static final String COMMAND_KEYWORD_OLDVALUE = "$OLDVALUE";
	public static final String APP_COMMAND_KEYWORD_PLANNED_VALUE = "$PLANNED_VALUE";

	public ReconcileCommon(BaseCommand base) {
		this.base = base;
	}

	/**
	 * This method will load data from AttributeRules.csv file. Command will be filled based on columnWithCommandString parameter
	 * @param columnWithCommandString
	 * @return
	 * @throws RequestFailure
	 */
	public Map<ResourceId, Map<String, ReconcileCommandData>> loadAttributesReconcileRules(AttributeRulesCSVStruct columnWithCommandString)
			throws RequestFailure {
		Configuration config = AdapterConfigurationManager.getAdapterConfiguration(base);

		// read user rules config file path
		String configFileLocation = config.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY);
		// read user rules config file name
		String customerRulesfileName = config
				.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RAW_ATTRIBUTE_RECONCILE_FILENAME_PROPERTY);

		// build map of ResourceId -> Map<Attribute name, Command>
		Map<ResourceId, Map<String, ReconcileCommandData>> userRulesMap = new HashMap<>();
		try (final CSVReader reader = new CSVReader(configFileLocation, customerRulesfileName)) {
			ReconcileAttributeBuilder attributeBuilder = new ReconcileAttributeBuilder();

			// use COMMAND_DELETE_ATTR enum as column with command string
			userRulesMap = attributeBuilder.readAttributesRules(reader, columnWithCommandString);
		} catch (IOException e) {
			RequestFailure ex = new RequestFailure(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}

		return userRulesMap;
	}

	public Map<ResourceId, ResourceRuleCommands> loadResourcesReconcileRules() throws RequestFailure {
		 Configuration config = AdapterConfigurationManager.getAdapterConfiguration(base);

		// read user rules config file path
		String configFileLocation = config.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_LOCATION_PROPERTY);
		// read user rules config file name
		String customerRulesfileName = config.getString(ReconcileCommon.CUSTOMER_CONFIGURATION_RESOURCE_RECONCILE_FILENAME_PROPERTY);
		
		try (final CSVReader reader = new CSVReader(configFileLocation, customerRulesfileName)) {
			ReconcileAttributeBuilder attributeBuilder = new ReconcileAttributeBuilder();

			// use COMMAND_DELETE_ATTR enum as column with command string
			return attributeBuilder.readResourcesRules(reader);
		} catch (IOException e) {
			RequestFailure ex = new RequestFailure(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
}
