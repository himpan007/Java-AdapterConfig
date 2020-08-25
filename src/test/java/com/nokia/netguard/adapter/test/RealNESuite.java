package com.nokia.netguard.adapter.test;

import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nokia.netguard.adapter.test.base.NE;
import com.nokia.netguard.adapter.test.suite.TestSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses("suite/realne/**/*.class")
public class RealNESuite extends TestSuite {

	@Override
	public NE[] getNetworkElements() {
		NE sshNE = new NE();
		sshNE.setIp("192.168.56.5");
		sshNE.setPort(22);
		sshNE.setConnectionProtocol(ConnectionProtocol.SSH);
		sshNE.setUser("tester");
		sshNE.setPassword("pass1!");
		sshNE.setAdapterDefinitionDir(".");
		
		NE telnetNE = new NE();
		telnetNE.setIp("192.168.56.5");
		telnetNE.setPort(23);
		telnetNE.setConnectionProtocol(ConnectionProtocol.TELNET);
		telnetNE.setUser("tester");
		telnetNE.setPassword("pass1!");
		telnetNE.setAdapterDefinitionDir(".");
		
		return new NE[] {sshNE, telnetNE};
	};
}
