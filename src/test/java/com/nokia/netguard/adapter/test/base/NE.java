package com.nokia.netguard.adapter.test.base;

import com.nakina.oss.shared.type.ConnectionProtocol;
import com.nokia.netguard.adapter.test.suite.TestSuite;

public class NE {
	private String ip = "";
	private int port = -1;
	private String user = "";
	private String password = "";
	private ConnectionProtocol connectionProtocol = null;
	private boolean isOfflineNE = false;
	private String offlineNEPrompt = "[user@host]:~$ ";
	private String adapterDefinitionDir = ".";

	public NE() {
	}
	
	public NE(String ip, int port, String user, String password) {
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public ConnectionProtocol getConnectionProtocol() {
		return connectionProtocol;
	}
	
	public void setConnectionProtocol(ConnectionProtocol connectinoProtocol) {
		this.connectionProtocol = connectinoProtocol;
	}
	
	public void setOfflineNEPrompt(String offlineNEPrompt) {
		this.offlineNEPrompt = offlineNEPrompt;
	}
	
	public String getOfflineNEPrompt() {
		return offlineNEPrompt;
	}
	
	public void setOfflineTest() {
		this.isOfflineNE = true;
		this.ip = TestSuite.OFFLINE_TEST;
	}
	
	public boolean isOfflineTest() {
		return this.ip.equals(TestSuite.OFFLINE_TEST);
	}

	public String getAdapterDefinitionDir() {
		return adapterDefinitionDir;
	}

	public void setAdapterDefinitionDir(String adapterDefinitionDir) {
		this.adapterDefinitionDir = adapterDefinitionDir;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isOfflineNE) {
			sb.append(TestSuite.OFFLINE_TEST);
			sb.append(", offlineNEPrompt: ").append(offlineNEPrompt);
		} else {
			sb.append("ip: ").append(ip);
		}
		if (port != -1) {
			sb.append(", port: ").append(port);
		}
		if (!user.isEmpty()) {
			sb.append(", user: ").append(user);
		}
		if (!password.isEmpty()) {
			sb.append(", password: ").append(password);
		}
		if (connectionProtocol != null) {
			sb.append(", connectionProtocol: ").append(connectionProtocol.getLabel());
		}
		sb.append(", adapterDefinitionDir: ").append(adapterDefinitionDir);
		return sb.toString();
	}

}
