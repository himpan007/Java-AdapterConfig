/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.attribute;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;
import java.util.Map.Entry;

import com.nakina.adapter.api.responsedata.inventory.resource.EditResourcesRawAttributesResponseData;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.EditResourcesRawAttributesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.EditResourcesRawAttributesRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.AttributeRulesCSVStruct;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;

/**
 * This adapter processes this request based on Customer Configuration Rules.
 * If there is no customer rules configured for this type of requests, raise RequestFailure.
 * 
 * For each resource/attribute in the request, the processing result (success or failure with proper message) will be set.
 * Failure cases: 
 *    there is no rule avaialbe for the resource/attribute
 *    there is a rule, but the excution of the command failed  
 * 
 * Success: there is a rule and the execution of the command completed
 * 
 * @author swang
 *
 */
public class EditResourceRawAttributesRequestHandler extends EditResourcesRawAttributesRequestHandlerBase {
	
	private EditResourcesRawAttributesResponseDataBuilder editAttrDataBuilder = null;	
	private Executor executor;	
	
	@Override
	public EditResourcesRawAttributesResponseData editResourcesRawAttributes(Map<ResourceId, Map<String, String>> resourcesRawAttributes)
			throws RequestFailure {
		 EditResourcesRawAttributesResponseDataBuilder respBuilder = updateResourcesRawAttributes(resourcesRawAttributes);
         return respBuilder.getResponseData();
	}

	/** 
	 * The base code should be fixed to use the ResponseBuilder. 
	 * @param resourceMap
	 * @return
	 * @throws RequestFailure
	 */
	public EditResourcesRawAttributesResponseDataBuilder updateResourcesRawAttributes(Map<ResourceId, Map<String, String>> resourceMap)
			throws RequestFailure {
		
		traceInfo(getClass(), LOG_PREFIX + "EditResourcesRawAttributes !!!");	

		executor = new Executor(this);
		
		// check if there are resources to proceed
		editAttrDataBuilder = new EditResourcesRawAttributesResponseDataBuilder();
		if (resourceMap == null || resourceMap.isEmpty())
			throw new RequestFailure("EditResourceRawAttributes resourceMap is empty.  " );
		
		// common class used for reconcile operations
		ReconcileCommon reconcileCommon = new ReconcileCommon(this);
		// load user rules using common class and pass column name 'COMMAND_UPDATE_ATTR' with command to call
		Map<ResourceId, Map<String, ReconcileCommandData>> userRulesMap = reconcileCommon
			.loadAttributesReconcileRules(AttributeRulesCSVStruct.COMMAND_UPDATE_ATTR);

		// iterate over provided resources to process
		for (Entry<ResourceId, Map<String, String>> entry : resourceMap.entrySet()) {
			// if resource id not existing in user rules add failure to responseDataBuilder
			if (!userRulesMap.containsKey(entry.getKey())) {
				String failure = "No Customer Rule found for updating attributes under resource  " + entry.getKey();
				traceError(this.getClass(), LOG_PREFIX + failure);
				editAttrDataBuilder.addRawAttributesFailureStatus(entry.getKey(),
						entry.getValue().keySet(), failure);
			} else {
				processAttributes(entry, userRulesMap.get(entry.getKey()));
			}
		}
		
		return editAttrDataBuilder;
	}    
	
	
	private void processAttributes(Entry<ResourceId, Map<String, String>> resourceEntry,
			Map<String, ReconcileCommandData> userRulesAttributesMap) throws RequestFailure {		
		ResourceId resource = resourceEntry.getKey();
		
		for (Entry<String, String> attributeEntry : resourceEntry.getValue().entrySet()) {
			String attr = attributeEntry.getKey();
			
			if (!userRulesAttributesMap.containsKey(attr)) {
				String failure = "No User Rules to support EditAttributes for resource " + resource + ", attribute=" + attr;
				traceInfo(this.getClass(), LOG_PREFIX + failure);
				editAttrDataBuilder.addRawAttributeFailureStatus(resourceEntry.getKey(),
						attr, failure);
				continue;
			}

			ReconcileCommandData commandData = userRulesAttributesMap.get(attr);
			String commandString = commandData.getCommand();
			
			if(attributeEntry.getValue() != null)
				commandString = commandData.getCommand().replace(ReconcileCommon.COMMAND_KEYWORD_ATTRVALUE, attributeEntry.getValue());
				
			/***
			 * execute command with ';echo $?' which will cause returning of command status
			 * code to output. Then we can check if command was successfully executed and
			 * set proper status in responseDataBuilder object
			 */
			ExecutionResult result = executor.execute(
					Command.create(commandString)
						.setTimeout(commandData.getTimeout())
						.retrieveExitCode());
			
			if (result.isSuccessful()) {
				editAttrDataBuilder.addRawAttributeSuccessfulStatus(resource, attr);
			} else {
				editAttrDataBuilder.addRawAttributeFailureStatus(resource, attr, result.getCleanOutput().trim());
			}			
		}
	}	
}
