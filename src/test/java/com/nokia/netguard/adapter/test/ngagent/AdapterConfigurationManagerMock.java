package com.nokia.netguard.adapter.test.ngagent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;

import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.RequestFailure;

public class AdapterConfigurationManagerMock {

	private static final String CONFIGURATION_FILE_NAME = "adapter-configuration.xml";
	private static CombinedConfiguration config;

	public static Configuration getAdapterConfiguration() throws RequestFailure {

		if (config == null) {
			config = new CombinedConfiguration(new OverrideCombiner());
			String exeDir = System.getProperty("user.dir");
			Finder finder = new Finder(CONFIGURATION_FILE_NAME, FileVisitResult.TERMINATE);
			try {
				Files.walkFileTree(Paths.get(exeDir), finder);
				Vector<String> matchList = finder.getMatchList();
				XMLConfiguration xmlConf = new XMLConfiguration();
				if (matchList.size() >= 1) {
					String xmlFilePath = matchList.get(0);
					xmlConf.load(Files.newInputStream(Paths.get(xmlFilePath)));
				}
				config.addConfiguration(xmlConf);
			} catch (ConfigurationException e) {
				String error = "Error parsing Adapter configuration file \'" + CONFIGURATION_FILE_NAME + "\': "
						+ e.getMessage();
				Trace.error(AdapterConfigurationManagerMock.class, error, e);
				throw new RequestFailure(error);
			} catch (IOException e) {
				String error = "Error by opening file: " + e.getMessage();
				Trace.error(AdapterConfigurationManagerMock.class, error, e);
				throw new RequestFailure(error);
			}
		}
		return config;
	}
}