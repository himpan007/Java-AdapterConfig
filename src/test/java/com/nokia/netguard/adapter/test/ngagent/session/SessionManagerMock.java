package com.nokia.netguard.adapter.test.ngagent.session;

import org.junit.Assert;

import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nokia.netguard.adapter.test.base.NE;

public abstract class SessionManagerMock {

	protected String m_host;
	protected String m_username;
	protected String m_password;
	protected Integer m_port;
	protected ConnectionProtocol m_connectionProtocol;
	protected int m_timeout = 30000; // 30 seconds
	protected boolean connected = false;

	public SessionManagerMock(String host, String username, String password, Integer port, int timeout) {
		m_host = host;
		m_username = username;
		m_password = password;
		m_port = port;
		m_timeout = timeout;
	}

	public SessionManagerMock(String host, String username, String password, Integer port) {
		m_host = host;
		m_username = username;
		m_password = password;
		m_port = port;
		m_timeout = 30000;
	}
	
	public SessionManagerMock(NE ne) {
		this.m_host = ne.getIp();
		this.m_username = ne.getUser();
		this.m_password = ne.getPassword();
		this.m_port = ne.getPort();
		this.m_connectionProtocol = ne.getConnectionProtocol();
		this.m_timeout = 30000;
	}

	public void connect() {
		if ((m_host == null) || m_host.isEmpty()) {
			Trace.error(this.getClass(), "Invalid host parameters!");
		}

		if ((m_username == null) || m_username.isEmpty()) {
			Trace.error(this.getClass(), "Invalid username parameters!");
		}

		if ((m_password == null) || m_password.isEmpty()) {
			Trace.error(this.getClass(), "Invalid password parameters!");
		}

		if ((m_port == null) || m_port.intValue() < 0) {
			Trace.error(this.getClass(), "Invalid port parameters!");
		}

		if (m_connectionProtocol.equals(ConnectionProtocol.SSH)) {
			connected = connectSsh();
		} else if (m_connectionProtocol.equals(ConnectionProtocol.TELNET)){
			connected = connectTelnet();
		} else {
			Assert.fail("NE ConnectionProtocol must be either SSH or Telnet!");
		}
	}

	public void disconnect() {
		if (m_connectionProtocol.equals(ConnectionProtocol.SSH)) {
			disconnectSsh();
		} else if (m_connectionProtocol.equals(ConnectionProtocol.TELNET)) {
			disconnectTelnet();
		} else {
			Assert.fail("NE ConnectionProtocol must be either SSH or Telnet!");
		}
		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public String getUsername() {
		return this.m_username;
	}

	public String getPassword() {
		return this.m_password;
	}

	public abstract SessionChannel getChannel();
	
	public abstract boolean connectSsh();
	
	public abstract boolean disconnectSsh();
	
	public abstract boolean connectTelnet();
	
	public abstract boolean disconnectTelnet();
}