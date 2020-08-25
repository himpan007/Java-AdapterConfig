package com.nokia.netguard.adapter.requests.gs.resource.retrieval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.csv.CSVRecord;

import com.nakina.adapter.api.responsedatabuilder.ResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.inventory.physical.ManagedElementResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.inventory.physical.PhysicalEquipmentResponseDataBuilder;
import com.nakina.adapter.api.responsedatabuilder.inventory.physical.RawAttributeBuilder;
import com.nakina.adapter.api.responsedatabuilder.inventory.resource.ResourceBuilder;
import com.nakina.adapter.api.type.inventory.common.ResourceMinimalId;
import com.nakina.adapter.base.agent.api.base.AdapterConfigurationManager;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.adapter.base.agent.api.inventory.resource.ResourceRawRetrievalInterface;
import com.nakina.adapter.base.agent.api.inventory.resource.ResourceRawRetrievalRequestHandlerBase;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.Executor;
import com.nokia.netguard.adapter.configuration.Constants;
import com.nokia.netguard.adapter.requests.gs.data.collecting.CSVReader;
import com.nokia.netguard.adapter.requests.gs.data.collecting.csvstruct.ReadRulesCSVStruct;

public class RetrieveResourceRawAttributesRequestHandler extends ResourceRawRetrievalRequestHandlerBase
		implements ResourceRawRetrievalInterface {

	public static final String ROOT_CONFIGURATION                                    = "Configuration";
	public static final String ROOT_MANAGED_ELEMENT                                  = "ManagedElement";
	public static final String ROOT_RESOURCE_TYPE                                    = "ManagedElement";
	public static final String CUSTOMER_CONFIGURATION_LOCATION_PROPERTY              = "interfaceType.cli.resourceRawDataConfigFile.location";
	public static final String CUSTOMER_CONFIGURATION_RAW_RETRIEVE_FILENAME_PROPERTY = "interfaceType.cli.resourceRawDataConfigFile.rawAttributeRetrieveRule";
	public static final String ADD_DEFAULT_USERS_PROPERTY                            = "defaultUsers.addDefaultUsers";
	public static final String DEFAULT_USERS_LIST_PROPERTY                           = "defaultUsers.defaultUsersList.user";

	@Override
	protected ResourceRawRetrievalInterface getRequestHandler() {
		return this;
	}

	@Override
	public List<ResponseDataBuilder> getAllRawResourceData() throws RequestFailure {
		List<ResponseDataBuilder> rawResponseDataList = new ArrayList<>();

		traceInfo(getClass(), Constants.LOG_PREFIX + "Getting RawResourceData !!!");
		ResourceBuilder baseResourceBuilder = new ResourceBuilder(ROOT_MANAGED_ELEMENT, ROOT_CONFIGURATION, ROOT_MANAGED_ELEMENT);
		rawResponseDataList.add(baseResourceBuilder);

		Configuration config      = AdapterConfigurationManager.getAdapterConfiguration(this);
		String configFileLocation = config.getString(CUSTOMER_CONFIGURATION_LOCATION_PROPERTY);
		String fileNameRead       = config.getString(CUSTOMER_CONFIGURATION_RAW_RETRIEVE_FILENAME_PROPERTY);
		
		traceInfo(getClass(), Constants.LOG_PREFIX + CUSTOMER_CONFIGURATION_LOCATION_PROPERTY + " " + configFileLocation);
		traceInfo(getClass(), Constants.LOG_PREFIX + CUSTOMER_CONFIGURATION_RAW_RETRIEVE_FILENAME_PROPERTY + " " + fileNameRead);

		Executor executor     = new Executor(this);
		StringBuilder builder = new StringBuilder();

		try (CSVReader reader = new CSVReader(configFileLocation, fileNameRead)) {
			for (final CSVRecord record : reader.read()) {
				executor.execute(Command.create(record.get(ReadRulesCSVStruct.COMMAND))
						.setTimeout(Integer.parseInt(record.get(ReadRulesCSVStruct.TIMEOUT_IN_MILLISECONDS)))
						.addCallback(executionResult -> builder.append(executionResult.getCleanOutput())));
				rawResponseDataList.add(buildResource(record, builder.toString()));
				builder.setLength(0);
			}
		} catch (IOException e) {
			RequestFailure ex = new RequestFailure(e.getMessage());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}

		traceInfo(getClass(), Constants.LOG_PREFIX + "Getting User Account Data");
		rawResponseDataList.addAll(RetrieveUsersRawAttributes.getAccountsRawAttributes(this, config));
		
		traceInfo(getClass(), Constants.LOG_PREFIX + "Getting RawResourceData Completed !!!");
		return rawResponseDataList;
	}

	public ResourceBuilder buildResource(CSVRecord record, String commandOutput) {
		ResourceBuilder resourceBuilder = new ResourceBuilder(record.get(ReadRulesCSVStruct.RESOURCE_ID), ROOT_CONFIGURATION,
				record.get(ReadRulesCSVStruct.RESOURCE_TYPE));
		resourceBuilder.setConfigResourceSubtype(record.get(ReadRulesCSVStruct.RESORUCE_SUBTYPE));
		resourceBuilder.setParentId(new ResourceMinimalId(ROOT_MANAGED_ELEMENT, ROOT_CONFIGURATION, ROOT_MANAGED_ELEMENT));

		resourceBuilder.addRawAttr(record.get(ReadRulesCSVStruct.ATTRIBUTE_NAME), commandOutput);
		return resourceBuilder;
	}

	@Override
	public List<ResponseDataBuilder> getCustomDataByResourceSubType(String resourceType, String resourceSubtype)
			throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public List<ResponseDataBuilder> getCustomDataByResourceType(String resourceType) throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public ResponseDataBuilder getCustomRawResourceData(String entityId, String entityType, String resourceType)
			throws RequestFailure {
		return new RawAttributeBuilder();
	}

	@Override
	public List<ResponseDataBuilder> getLogicalInventoryDataByResourceSubType(String resourceType,
			String resourceSubtype) throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public List<ResponseDataBuilder> getLogicalInventoryDataByResourceType(String resourceType) throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public ResponseDataBuilder getLogicalInventoryRawResourceData(String entityId, String entityType,
			String resourceType) throws RequestFailure {
		return new RawAttributeBuilder();
	}

	@Override
	public List<PhysicalEquipmentResponseDataBuilder> getPhysicalInventoryDataByResourceSubType(String resourceType,
			String resourceSubtype) throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public List<PhysicalEquipmentResponseDataBuilder> getPhysicalInventoryDataByResourceType(String resourceType)
			throws RequestFailure {
		return Collections.emptyList();
	}

	@Override
	public PhysicalEquipmentResponseDataBuilder getPhysicalInventoryRawResourceData(String entityId, String entityType,
			String resourceType) throws RequestFailure {

		// NOTE: ManagedElementResponseDataBuilder is just an example builder that could
		// be
		// used for this method. Select a builder that fits the type of resource that is
		// being retrieved.

		return new ManagedElementResponseDataBuilder();
	}
}
