/**
 * 
 */
package com.nokia.netguard.adapter.requests.gs.reconcile.command;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;

import org.apache.commons.lang.Validate;

import com.nakina.adapter.api.responsedatabuilder.inventory.resource.CommandResourceResponseDataBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceId;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.CommandResourceRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;

/**
 * This adapter processes this request based on the command passed in. The
 * command execution result (success or failure with proper message) will be set
 * to the response.
 * 
 * @author adskoczy
 *
 */
public class CommandResourceRequestHandler extends CommandResourceRequestHandlerBase {
	@Override
	public CommandResourceResponseDataBuilder executeCommandResource(ResourceId resourceId, String command)
			throws RequestFailure {

		traceDebug(this.getClass(), LOG_PREFIX + "executeCommandResource");
		traceDebug(this.getClass(), LOG_PREFIX + "resourceId: " + resourceId + ",command: " + command);
		validateArguments(resourceId, command);

		CommandResourceResponseDataBuilder commandResoureDataBuilder = new CommandResourceResponseDataBuilder();

		// Send command and update response
		String cmdName = "APP command resource '" + resourceId + "' ";
		Executor executor = new Executor(this);
		ExecutionResult result = executor.execute(Command.create(command).setName(cmdName).retrieveExitCode());
		if (result.isSuccessful()) {
			commandResoureDataBuilder.setResourceSuccessfulStatus();
		} else {
			String failure = result.getCleanOutput().trim();
			commandResoureDataBuilder.setResourceFailureStatus(failure);
			traceError(getClass(), LOG_PREFIX + "command failed. Fail message: " + failure);
		}
		traceDebug(getClass(), LOG_PREFIX + "executeCommandResource Completed !!!");
		return commandResoureDataBuilder;
	}

	private void validateArguments(ResourceId resourceId, String command) throws RequestFailure {
		try {
			Validate.notNull(resourceId, "ResourceId should not be null.");
			Validate.notNull(command, "Command should not be null.");
		} catch (Exception e) {
			throw new RequestFailure("Request Arguments Validation Failed: " + e.getMessage());
		}
	}
}
