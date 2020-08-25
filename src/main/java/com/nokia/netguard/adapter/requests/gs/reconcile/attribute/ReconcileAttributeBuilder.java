package com.nokia.netguard.adapter.requests.gs.reconcile.attribute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.requests.gs.data.collecting.CSVReader;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.AttributeRulesCSVStruct;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.ResourceRulesCSVStruct;
import com.nokia.netguard.adapter.requests.gs.reconcile.resource.ResourceRuleCommands;

public class ReconcileAttributeBuilder {
	public Map<ResourceId, Map<String, ReconcileCommandData>> readAttributesRules(CSVReader reader,
			AttributeRulesCSVStruct command) throws RequestFailure {
		Map<ResourceId, Map<String, ReconcileCommandData>> userRulesMap = new HashMap<>();

		try {
			for (CSVRecord entry : reader.read()) {
				// if command empty set skip entry
				if (!entry.isSet(command.name()) || entry.get(command).isEmpty()) {
					continue;
				}

				ResourceId resource = buildResourceId(entry);

				Map<String, ReconcileCommandData> attrs;
				if (!userRulesMap.containsKey(resource)) {
					userRulesMap.put(resource, new HashMap<>());
				}

				attrs = userRulesMap.get(resource);

				String attribute = entry.get(AttributeRulesCSVStruct.ATTRIBUTE_NAME.name());

				String commandString = entry.get(command.name());
				if (!attrs.containsKey(attribute)) {
					attrs.put(attribute, new ReconcileCommandData(commandString,
							Integer.parseInt(entry.get(AttributeRulesCSVStruct.TIMEOUT_IN_MILLISECONDS.name()))));
				} else {
					throw new RequestFailure(
							"attribute: " + attribute + " for resource: " + resource + " already exist");
				}
			}
		} catch (IOException e) {
			RequestFailure ex = new RequestFailure(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}

		return userRulesMap;
	}

	private ResourceId buildResourceId(CSVRecord entry) {
		return new ResourceId(entry.get(AttributeRulesCSVStruct.RESOURCE_ID.name()),
				entry.get(AttributeRulesCSVStruct.RESOURCE_SUBTYPE.name()),
				entry.get(AttributeRulesCSVStruct.RESOURCE_TYPE.name()));
	}

	/**
	 * structure <ResourceID, ResourceRuleCommands>
	 * 
	 * @param reader
	 * @param commandCreateResource
	 * @param commonCommandcreateAttributes
	 * @return
	 * @throws RequestFailure
	 */
	public Map<ResourceId, ResourceRuleCommands> readResourcesRules(CSVReader reader) throws RequestFailure {
		Map<ResourceId, ResourceRuleCommands> userRulesMap = new HashMap<>();

		try {
			for (CSVRecord entry : reader.read()) {
				ResourceId resource = buildResourceId(entry);

				if (userRulesMap.containsKey(resource)) {
					throw new RequestFailure("Resource already exist in rules. " + resource);
				}

				ResourceRuleCommands resourceData = new ResourceRuleCommands(
						entry.get(ResourceRulesCSVStruct.COMMAND_CREATE_RESOURCE.name()),
						entry.get(ResourceRulesCSVStruct.COMMON_COMMANDCREATE_ATTRIBUTES.name()),
						entry.get(ResourceRulesCSVStruct.COMMAND_DELETE_RESOURCE.name()));

				userRulesMap.put(resource, resourceData);
			}
		} catch (IOException e) {
			RequestFailure ex = new RequestFailure(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}

		return userRulesMap;
	}
}
