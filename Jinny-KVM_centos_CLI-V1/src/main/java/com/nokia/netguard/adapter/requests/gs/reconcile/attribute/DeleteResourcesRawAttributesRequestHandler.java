package com.nokia.netguard.adapter.requests.gs.reconcile.attribute;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.DeleteResourcesRawAttributesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.DeleteResourcesRawAttributesRequestHandlerBase;
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
 * available for the resource/attribute there is a rule, but the excution of the
 * command failed
 * 
 * Success: there is a rule and the execution of the command completed
 * 
 * @author adskoczy
 *
 */
public class DeleteResourcesRawAttributesRequestHandler extends DeleteResourcesRawAttributesRequestHandlerBase {
	private Executor executor;
	private DeleteResourcesRawAttributesResponseDataBuilder deleteAttrDataBuilder;

	@Override
	public DeleteResourcesRawAttributesResponseDataBuilder deleteResourcesRawAttributes(
			Map<ResourceId, Set<String>> resourceMap) throws RequestFailure {
		executor = new Executor(this);

		traceInfo(getClass(), LOG_PREFIX + "DeleteResourcesRawAttributes !!!");

		deleteAttrDataBuilder = new DeleteResourcesRawAttributesResponseDataBuilder();
		if (resourceMap == null || resourceMap.isEmpty())
			throw new RequestFailure("DeleteResourcesRawAttributes resourceMap is empty.  ");

		ReconcileCommon reconcileCommon = new ReconcileCommon(this);
		Map<ResourceId, Map<String, ReconcileCommandData>> userRulesMap = reconcileCommon
				.loadAttributesReconcileRules(AttributeRulesCSVStruct.COMMAND_DELETE_ATTR);

		for (Entry<ResourceId, Set<String>> resourceEntry : resourceMap.entrySet()) {
			ResourceId resource = resourceEntry.getKey();

			// return failure status if resource not exist in user rules in csv file
			if (!userRulesMap.containsKey(resource)) {
				String failure = "No Customer Rule found for updating attributes under resource  " + resource;
				traceInfo(this.getClass(), LOG_PREFIX + failure);
				deleteAttrDataBuilder.addRawAttributesFailureStatus(resource, resourceEntry.getValue(), failure);
				continue;
			}
			
			// call commands for attributes
			processAttributes(resourceEntry.getKey(), resourceEntry.getValue(), userRulesMap.get(resource));
		}

		traceInfo(getClass(), LOG_PREFIX + "DeleteResourcesRawAttributes Completed !!!");
		return deleteAttrDataBuilder;
	}

	private void processAttributes(ResourceId resource, Set<String> resourceAttributes,
			Map<String, ReconcileCommandData> userRulesAttributes) throws RequestFailure {
		for (String attr : resourceAttributes) {
			// return failure status if attribute not found in user rules in csv file
			if (!userRulesAttributes.containsKey(attr)) {
				String errorMsg = "There is no user rule found for attribute: " + attr;
				traceError(getClass(), LOG_PREFIX + errorMsg);
				deleteAttrDataBuilder.addRawAttributeFailureStatus(resource, attr, errorMsg);
			} else {
				/***
				 * execute command with ';echo $?' which will cause returning of command status
				 * code to output. Then we can check if command was successfully executed and
				 * set proper status in responseDataBuilder object
				 */
				ReconcileCommandData commandData = userRulesAttributes.get(attr);
				
				ExecutionResult result = executor.execute(
						Command.create(commandData.getCommand())
						.setTimeout(commandData.getTimeout())
						.retrieveExitCode());
				
				if (result.isSuccessful()) {
					deleteAttrDataBuilder.addRawAttributeSuccessfulStatus(resource, attr);
				} else {
					deleteAttrDataBuilder.addRawAttributeFailureStatus(resource, attr,result.getCleanOutput().trim());
				}
			}
		}
	}
}
