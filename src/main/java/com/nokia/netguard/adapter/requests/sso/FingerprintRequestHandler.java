package com.nokia.netguard.adapter.requests.sso;

import static com.nokia.netguard.adapter.configuration.Constants.LOG_PREFIX;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nakina.adapter.api.responsedata.sessionmanagement.FingerprintResponseData;
import com.nakina.adapter.base.agent.api.adapterConfiguration.AdapterConfiguration;
import com.nakina.adapter.base.agent.api.associationmgmt.FingerprintStrategyBase;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nakina.oss.shared.type.ManagementProtocol;
import com.nokia.netguard.adapter.cli.Command;
import com.nokia.netguard.adapter.cli.ExecutionResult;
import com.nokia.netguard.adapter.cli.Executor;

/**
 * Returns a FingerprintResponseData that contains the Vendor, Model and Version
 * for the NE. The system then compares this to the Vendor, Model and NeVersion
 * pattern in the adapterInfo.xml to see if this NE can be managed by this
 * adapter.
 */
public class FingerprintRequestHandler extends FingerprintStrategyBase {
	private static final String NOT_SET = "NOT_SET";

	private static final String FINGERPRINT_COMMAND_PROPERTY = "fingerprintRules.%s.neCommand";
	private static final String FINGERPRINT_PATTERN_PROPERTY = "fingerprintRules.%s.pattern";

	private final static String VENDOR = "vendor";
	private final static String MODEL = "model";
	private final static String VERSION = "version";
	private final static String[] FIELDS_TO_SET = new String[]{VENDOR, MODEL, VERSION};
	private AdapterConfiguration config;

	public FingerprintRequestHandler() {
		super();
	}

	@Override
	public FingerprintResponseData getFingerPrintData() throws RequestFailure {
		ManagementProtocol managementProtocol = getManagementProtocol();

		traceInfo(getClass(), LOG_PREFIX + "Fingerprint Request for Interface: " + managementProtocol.toString());
		FingerprintResponseData frd = null;

		config = new AdapterConfiguration(this);

		if (managementProtocol.equals(ManagementProtocol.CLI)) {
			frd = cliFingerprint();
		} else {
			traceError(getClass(), "Unsupported management protocol: " + managementProtocol.toString());
			throw new RequestFailure("The Fingerprint_REQ failed as the management protocol is not supported: "
					+ managementProtocol.toString());
		}

		return frd;
	}

	private FingerprintResponseData cliFingerprint() throws RequestFailure {
		String expectedVendor = config.getStringValue(VENDOR, NOT_SET);
		String expectedModel = config.getStringValue(MODEL, NOT_SET);
		String expectedVersion = config.getStringValue(VERSION, NOT_SET);

		if (expectedVendor == null || expectedModel == null || expectedVersion == null) {
			throw new RequestFailure("Missing Adapter FPD configuration. ");
		}

		FingerprintResponseData frd = new FingerprintResponseData();
		Executor executor = new Executor(this);

		for (String field : FIELDS_TO_SET) {
			String cmd = config.getStringValue(String.format(FINGERPRINT_COMMAND_PROPERTY, field), NOT_SET);
			String pattern = config.getStringValue(String.format(FINGERPRINT_PATTERN_PROPERTY, field), NOT_SET);

			traceInfo(getClass(), LOG_PREFIX + "Execute fingerprint command for " + field);
			ExecutionResult result = executor.execute(Command.create(cmd).retrieveExitCode());

			Pattern compiledPattern = Pattern.compile(pattern, DOTALL);
			Matcher matcher = compiledPattern.matcher(result.getCleanOutput());
			if (matcher.matches()) {
				switch (field) {
				case VENDOR:
					frd.setVendor(expectedVendor);
					break;
				case MODEL:
					frd.setModel(expectedModel);
					break;
				case VERSION:
					frd.setVersion(expectedVersion);
					break;
				}
			} else {
				switch (field) {
				case VENDOR:
					frd.setVendor(result.getCleanOutput());
					break;
				case MODEL:
					frd.setModel(result.getCleanOutput());
					break;
				case VERSION:
					frd.setVersion(result.getCleanOutput());
					break;
				}
			}
		}

		return frd;
	}
}
