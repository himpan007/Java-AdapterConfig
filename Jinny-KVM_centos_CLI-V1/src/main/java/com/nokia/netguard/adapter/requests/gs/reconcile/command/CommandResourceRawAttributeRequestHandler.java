/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.command;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import org.apache.commons.lang.Validate;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CommandResourceRawAttributeResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.CommandResourceRawAttributeRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.requests.gs.reconcile.ReconcileCommon;

/**
 * This adapter processes this request based on the command passed in. The
 * command execution result (success or failure with proper message) will be set
 * to the response.
 * 
 * @author swang
 *
 */
public class CommandResourceRawAttributeRequestHandler extends CommandResourceRawAttributeRequestHandlerBase {
	@Override
	public CommandResourceRawAttributeResponseDataBuilder executeCommandResourceRawAttribute(ResourceId resourceId,
			String attributeName, String attributeValue, String command) throws RequestFailure {

		traceDebug(this.getClass(), LOG_PREFIX + "executeCommandResourceRawAttribute");

		traceDebug(this.getClass(),
				LOG_PREFIX + "CommandResourceRawAttributeRequestHandler: " + "resourceId: " + resourceId
						+ ",  attributeName: " + attributeName + ", attributeValue: " + attributeValue + ", command: "
						+ command);

		validateArguments(resourceId, attributeName, attributeValue, command);
		CommandResourceRawAttributeResponseDataBuilder responseDataBuilder = new CommandResourceRawAttributeResponseDataBuilder();

		// Send command and update response
		String cmdName = "APP command resource '" + resourceId + "' , attribute '" + attributeName + "' ";

		Executor executor = new Executor(this);
		ExecutionResult result = executor.execute(Command
				.create(command.replace(ReconcileCommon.APP_COMMAND_KEYWORD_PLANNED_VALUE, attributeValue))
				.setName(cmdName)
				.retrieveExitCode());

		if (result.isSuccessful()) {
			responseDataBuilder.setRawAttributeSuccessfulStatus();
		} else {
			String failure = result.getCleanOutput().trim();
			responseDataBuilder.setRawAttributeFailureStatus(failure);
			traceError(getClass(), LOG_PREFIX + "CommandResourceRawAttributeRequestHandler fail, message: " + failure);
		}
		traceInfo(getClass(), LOG_PREFIX + "CommandResourceRawAttributeRequest Completed !!!");
		return responseDataBuilder;
	}

	private void validateArguments(ResourceId resourceId, String attributeName, String attributeValue, String command)
			throws RequestFailure {
		try {
			Validate.notNull(resourceId, "ResourceId should not be null.");
			Validate.notNull(command, "Command should not be null.");
			Validate.notNull(attributeName, "AttributeName should not be null.");
			Validate.notNull(attributeValue, "AttributeValue should not be null.");
		} catch (Exception e) {
			throw new RequestFailure("Request Arguments Validation Failed: " + e.getMessage());
		}
	}

}
