/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.attribute;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;
import java.util.Map.Entry;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CreateResourcesRawAttributesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.CreateResourcesRawAttributesRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.AttributeRulesCSVStruct;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;

/**
 * This adapter processes this request based on Customer Configuration Rules. If
 * there is no customer rules configured for this type of requests, raise
 * RequestFailure.
 * 
 * For each resource/attribute in the request, the processing result (success or
 * failure with proper message) will be set. Failure cases: there is no rule
 * avaialbe for the resource/attribute there is a rule, but the excution of the
 * command failed
 * 
 * Success: there is a rule and the execution of the command completed
 * 
 * @author adskoczy
 *
 */
public class CreateResourcesRawAttributesRequestHandler extends CreateResourcesRawAttributesRequestHandlerBase {
	private CreateResourcesRawAttributesResponseDataBuilder createAttrDataBuilder = null;
	private Executor executor = null;

	public CreateResourcesRawAttributesResponseDataBuilder createResourcesRawAttributes(
			Map<ResourceId, Map<String, String>> resourceMap) throws RequestFailure {

		traceDebug(getClass(), LOG_PREFIX + "createResourcesRawAttributes");

		executor = new Executor(this);
		createAttrDataBuilder = new CreateResourcesRawAttributesResponseDataBuilder();

		// check if there are resources to proceed
		if (resourceMap == null || resourceMap.isEmpty())
			throw new RequestFailure("CreateResourcesRawAttributes resourceMap is empty.  ");

		// common class used for reconcile operations
		ReconcileCommon reconcileCommon = new ReconcileCommon(this);
		// load user rules using common class and pass column name 'COMMAND_CREATE_ATTR'
		// with command to call
		Map<ResourceId, Map<String, ReconcileCommandData>> userRulesMap = reconcileCommon
				.loadAttributesReconcileRules(AttributeRulesCSVStruct.COMMAND_CREATE_ATTR);

		// iterate over provided resources to process
		for (Entry<ResourceId, Map<String, String>> entry : resourceMap.entrySet()) {
			// if resource id not existing in user rules add failure to responseDataBuilder
			if (!userRulesMap.containsKey(entry.getKey())) {
				String failure = "No Customer Rule found for creating attribute under resource  " + entry.getKey();
				traceError(this.getClass(), LOG_PREFIX + failure);
				createAttrDataBuilder.addRawAttributesFailureStatus(entry.getKey(), entry.getValue().keySet(), failure);
			} else {
				processAttributes(entry, userRulesMap.get(entry.getKey()));
			}
		}

		return createAttrDataBuilder;
	}

	private void processAttributes(Entry<ResourceId, Map<String, String>> resourceEntry,
			Map<String, ReconcileCommandData> userRulesAttributesMap) throws RequestFailure {
		ResourceId resource = resourceEntry.getKey();
		
		for (Entry<String, String> attributeEntry : resourceEntry.getValue().entrySet()) {
			String attr = attributeEntry.getKey();

			if (!userRulesAttributesMap.containsKey(attr)) {
				String failure = "No Customer Rule found for creating attribute under resource " + resource
						+ ", attribute=" + attr;
				traceInfo(this.getClass(), LOG_PREFIX + failure);
				createAttrDataBuilder.addRawAttributeFailureStatus(resourceEntry.getKey(), attr,
						failure);
				continue;
			}

			ReconcileCommandData commandData = userRulesAttributesMap.get(attr);
			String commandString = commandData.getCommand();
			if (attributeEntry.getValue() != null)
				commandString = commandString.replace(ReconcileCommon.COMMAND_KEYWORD_ATTRVALUE,
						attributeEntry.getValue());

			/***
			 * execute command with ';echo $?' which will cause returning of command status
			 * code to output. Then we can check if command was successfully executed and
			 * set proper status in responseDataBuilder object
			 */
			ExecutionResult result = executor
					.execute(Command.create(commandString).setTimeout(commandData.getTimeout()).retrieveExitCode());

			if (result.isSuccessful()) {
				createAttrDataBuilder.addRawAttributeSuccessfulStatus(resource, attr);
			} else {
				createAttrDataBuilder.addRawAttributeFailureStatus(resource, attr, result.getCleanOutput());
			}
		}
	}
}
