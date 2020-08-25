/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.resource;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CreateResourceResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.CreateResourceRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;

/**
 * This adapter processes this request based on Customer Configuration Rules. If
 * there is no customer rules configured for this type of requests, raise
 * RequestFailure.
 * 
 * If there is no customer rule for this resource, no operation can be run,
 * raise RequestFailure. For the resource, the processing result (success or
 * failure with proper message) will be set. If resource operation completed
 * success, continue the operations on given attributes, the processing result
 * (success or failure with proper message) will be set. Failure cases: there is
 * no rule avaialbe for processing rule/command is available, but the excution
 * of the command failed
 * 
 * Success: rule exists and the execution of the command completed
 * 
 * @author adskoczy
 *
 */
public class CreateResourceRequestHandler extends CreateResourceRequestHandlerBase {
	@Override
	public CreateResourceResponseDataBuilder createResource(ResourceId resourceId, Map<String, String> attributes,
			ResourceId parentResourceId) throws RequestFailure {		
		traceDebug(getClass(), LOG_PREFIX + "createResource");
		traceDebug(getClass(), LOG_PREFIX + "resourceId: " + resourceId + ", attributes: "
				+ attributes + ", parentResourceId: " + parentResourceId);
		
		validateArguments(resourceId);

		CreateResourceResponseDataBuilder creatResourceDataBuilder = new CreateResourceResponseDataBuilder();
		
		// common class used for reconcile operations
		ReconcileCommon reconcileCommon = new ReconcileCommon(this);
		// load user rules using common class and pass column name 'COMMAND_CREATE_ATTR' with command to call
		Map<ResourceId, ResourceRuleCommands> userRulesMap = reconcileCommon
						.loadResourcesReconcileRules();

		ResourceRuleCommands commands = userRulesMap.get(resourceId);
		Executor executor = new Executor(this);
		ExecutionResult result = executor.execute(Command.create(commands.getCommandCreateResource())
				.retrieveExitCode());
		
		if(!result.isSuccessful()) {
			String failure = result.getCleanOutput();
			creatResourceDataBuilder.setResourceFailureStatus(failure);
			traceError(getClass(), LOG_PREFIX + "createResourceByRule failed: " + failure);
			throw new RequestFailure("createResourceByRule failed: " + failure);
		} 
		
		creatResourceDataBuilder.setResourceSuccessfulStatus();
		creatResourceDataBuilder.setResourceId(resourceId);
		
		for(Entry<String, String> attribute : attributes.entrySet()) {
			String command = commands.getCommonCommandCreateAttributes()
					.replace(ReconcileCommon.COMMAND_KEYWORD_ATTRNAME, attribute.getKey())
					.replace(ReconcileCommon.COMMAND_KEYWORD_ATTRVALUE, attribute.getValue());
			
			result = executor.execute(Command.create(command).retrieveExitCode());
			if (result.isSuccessful()) {
				creatResourceDataBuilder.addRawAttributeSuccessfulStatus(attribute.getKey());
			} else {
				String failure = result.getCleanOutput().trim();
				creatResourceDataBuilder.addRawAttributeFailureStatus(attribute.getKey(), failure);
				traceError(getClass(), "attribute: " + attribute + " failed: " + failure);
			}
		}
								
		return creatResourceDataBuilder;
	}

	private void validateArguments(ResourceId resourceId)
			throws RequestFailure {
		try {
			Validate.notNull(resourceId, "ResourceId should not be null. ");
		} catch (Exception e) {
			throw new RequestFailure("CreateResource Request Arguments Validation Failed: Missing resourceId. ");
		}
	}
}
