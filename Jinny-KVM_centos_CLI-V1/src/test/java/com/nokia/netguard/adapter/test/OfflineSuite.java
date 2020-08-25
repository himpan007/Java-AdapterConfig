package com.nokia.netguard.adapter.test;

import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.TestSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses("suite/offline/**/*.class")
public class OfflineSuite extends TestSuite {

	@Override
	public NE[] getNetworkElements() {
		NE offlineNE = new NE();

		offlineNE.setOfflineTest();
		offlineNE.setUser("test");
		offlineNE.setPassword("test");
		
		return new NE[] {offlineNE};
	};
}
