package com.nokia.netguard.adapter.test.suite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;

import com.nokia.netguard.adapter.test.base.NE;

@RunWith(Suite.class)
public abstract class TestSuite {
	
	public static final String OFFLINE_TEST = "offline-test";

	/**
	 * Each Network Element, on which tests are to be executed, should be defined as follows:<br/><br/>
	 * 
	 * <code>new NE("[ip]", [port], "[username]", "[password]"),</code><br/><br/>
	 * 
	 * there is special keyword <code>offline-test</code> to be used as <code>[ip]</code>, which makes particular NE run as mock (offline test, without actual ssh connection). In that case, the <code>[port]</code> value is ignored.
	 * 
	 */
	@Parameters
	public Collection<NE[]> data() {
		
		List<NE[]> neList = new ArrayList<>();
		for (NE ne : getNetworkElements()) {
			neList.add(new NE[] {ne});
		}
		return neList;
	}

	public abstract NE[] getNetworkElements();

}
