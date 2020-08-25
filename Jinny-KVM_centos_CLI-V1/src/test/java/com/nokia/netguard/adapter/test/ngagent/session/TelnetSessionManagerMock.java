package com.nokia.netguard.adapter.test.ngagent.session;

import java.io.IOException;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.junit.Assert;

import com.nakina.adapter.api.shared.util.Trace;
import com.nokia.netguard.adapter.test.base.NE;

public class TelnetSessionManagerMock extends SessionManagerMock {

	private TelnetClient client;
	
	public TelnetSessionManagerMock(NE ne) {
		super(ne);
	}

	@Override
	public boolean connectTelnet() {
		try {
			client = new TelnetClient();
			client.addOptionHandler(new TerminalTypeOptionHandler("VT100", false, false, true, false));
			client.addOptionHandler(new EchoOptionHandler(true, false, true, false));
			client.addOptionHandler(new SuppressGAOptionHandler(true, true, true, true));
			client.connect(m_host, m_port);
		} catch (IOException | InvalidTelnetOptionException e) {
			Trace.error(getClass(), "Failed to connectTelnet()", e);
			Assert.fail();
		}
		return true;
	}

	@Override
	public boolean disconnectTelnet() {
		try {
			client.disconnect();
		} catch (IOException e) {
			Trace.error(getClass(), "Failed to disconnectTelnet()", e);
			Assert.fail();
		}
		return true;
	}

	@Override
	public SessionChannel getChannel() {
		return new SessionChannel(client);
	}

	@Override
	public boolean connectSsh() {
		return false;
	}

	@Override
	public boolean disconnectSsh() {
		return false;
	}

}
