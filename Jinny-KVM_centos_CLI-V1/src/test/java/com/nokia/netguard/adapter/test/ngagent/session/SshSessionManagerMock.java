package com.nokia.netguard.adapter.test.ngagent.session;

import org.junit.Assert;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nakina.adapter.api.shared.util.Trace;
import com.nokia.netguard.adapter.test.base.NE;

public class SshSessionManagerMock extends SessionManagerMock {

	private Session m_session = null;
	private ChannelShell m_channel = null;

	public SshSessionManagerMock(NE ne) {
		super(ne);
	}

	private Session getSession() {
		if (m_session == null || !m_session.isConnected()) {
			connect();
		}
		return m_session;
	}

	public SessionChannel getChannel() {
		if (m_channel == null || !m_channel.isConnected()) {
			try {
				m_channel = (ChannelShell)getSession().openChannel("shell");
				m_channel.connect();
			} catch (Exception e) {
				Trace.error(getClass(), "Error while opening channel:!!", e);
			}
		}
		return new SessionChannel(m_channel);
	}

	@Override
	public boolean connectSsh() {
		try {
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			m_session = jsch.getSession(m_username, m_host, m_port.intValue());
			m_session.setServerAliveInterval(1000);
			m_session.setPassword(m_password);
			m_session.setConfig(config);
			Trace.info(this.getClass(), "Connecting SSH to " + m_host + " with username " + m_username
					+ " and password " + m_password + " on port " + m_port + "\n");
			
			m_session.connect(m_timeout); // 30 seconds timeout
		} catch (JSchException e) {
			Trace.error(getClass(), "Failed to connectSsh()", e);
			Assert.fail();
		}
		return true;
	}

	@Override
	public boolean disconnectSsh() {
		Trace.info(this.getClass(), "Disconnecting from SSH Channel!... ");
		if (m_channel != null)
			m_channel.disconnect();
		Trace.info(this.getClass(), "Disconnecting from remote host: " + m_host);
		m_session.disconnect();
		return true;
	}

	@Override
	public boolean disconnectTelnet() {
		return false;
	}

	@Override
	public boolean connectTelnet() {
		return false;
	}
	
}
