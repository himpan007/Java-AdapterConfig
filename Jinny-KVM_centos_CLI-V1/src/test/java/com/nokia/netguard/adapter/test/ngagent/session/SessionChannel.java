package com.nokia.netguard.adapter.test.ngagent.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.telnet.TelnetClient;

import com.jcraft.jsch.Channel;

public class SessionChannel {

	private Channel sshChannel = null;
	private TelnetClient telnetChannel = null;
	
	public SessionChannel(Object channel) {
		if (channel instanceof Channel) {
			this.sshChannel = (Channel)channel;
		} else if (channel instanceof TelnetClient) {
			this.telnetChannel = (TelnetClient)channel;
		}
	}

	public Channel getSshChannel() {
		return sshChannel;
	}

	public TelnetClient getTelnetChannel() {
		return telnetChannel;
	}
	
	public InputStream getInputStream() {
		try {
			if (sshChannel != null) {
				return sshChannel.getInputStream();
			} else {
				return telnetChannel.getInputStream();
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	public OutputStream getOutputStream() {
		try {
			if (sshChannel != null) {
				return sshChannel.getOutputStream();
			} else {
				return telnetChannel.getOutputStream();
			}
		} catch (IOException e) {
			return null;
		}
	}
}
