/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.resource;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.DeleteResourcesResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.DeleteResourcesRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;

/**
 * This adapter processes this request based on Customer Configuration Rules. If
 * there is no customer rules configured for this type of requests, raise
 * RequestFailure.
 * 
 * For each resource in the request, the processing result (success or failure
 * with proper message) will be set. Failure cases: there is no rule available
 * for the resource there is a rule, but the execution of the command failed
 * 
 * Success: rule exists and the execution of the command completed
 * 
 * @author adskoczy
 *
 */
public class DeleteResourcesRequestHandler extends DeleteResourcesRequestHandlerBase {

	@Override
	public DeleteResourcesResponseDataBuilder deleteResources(Set<ResourceId> resources) throws RequestFailure {

		traceInfo(getClass(), LOG_PREFIX + "DeleteResources !!!");
		validateArguments(resources);

		DeleteResourcesResponseDataBuilder deleteResourcesDataBuilder = new DeleteResourcesResponseDataBuilder();

		// common class used for reconcile operations
		ReconcileCommon reconcileCommon = new ReconcileCommon(this);
		// load user rules using common class and pass column name 'COMMAND_CREATE_ATTR'
		// with command to call
		Map<ResourceId, ResourceRuleCommands> userRulesMap = reconcileCommon.loadResourcesReconcileRules();
		Executor executor = new Executor(this);

		for (ResourceId resourceId : resources) {
			ResourceRuleCommands commands = userRulesMap.get(resourceId);
			if (commands == null) {
				String failure = "No Customer Rule found for deleting resource " + resourceId;
				traceInfo(this.getClass(), LOG_PREFIX + failure);
				deleteResourcesDataBuilder.addResourceFailureStatus(resourceId, failure);
			} else {
				ExecutionResult result = executor
						.execute(Command.create(commands.getCommandDeleteResource()).retrieveExitCode());

				if (result.isSuccessful()) {
					deleteResourcesDataBuilder.addResourceSuccessfulStatus(resourceId);
				} else {
					deleteResourcesDataBuilder.addResourceFailureStatus(resourceId, result.getCleanOutput().trim());
				}
			}
		}
		
		return deleteResourcesDataBuilder;
	}

	private void validateArguments(Set<ResourceId> resources) throws RequestFailure {
		try {
			Validate.notEmpty(resources, "ResourceIds should not be null or empty. ");
		} catch (Exception e) {
			throw new RequestFailure("Request Arguments Validation Failed:  " + e.getMessage());
		}
	}

}
