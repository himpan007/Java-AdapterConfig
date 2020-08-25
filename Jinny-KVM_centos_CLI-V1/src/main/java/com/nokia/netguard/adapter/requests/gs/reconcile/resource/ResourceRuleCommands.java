package com.nokia.netguard.adapter.requests.gs.reconcile.resource;

public class ResourceRuleCommands {
	private String commandCreateResource;
	private String commonCommandCreateAttributes;
	private String commandDeleteResource;

	public ResourceRuleCommands(String commandCreateResource, String commonCommandCreateAttributes,
			String commandDeleteResource) {
		super();
		this.commandCreateResource = commandCreateResource;
		this.commonCommandCreateAttributes = commonCommandCreateAttributes;
		this.commandDeleteResource = commandDeleteResource;
	}

	public String getCommandCreateResource() {
		return commandCreateResource;
	}

	public String getCommonCommandCreateAttributes() {
		return commonCommandCreateAttributes;
	}

	public String getCommandDeleteResource() {
		return commandDeleteResource;
	}

}
