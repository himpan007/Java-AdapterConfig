package com.nokia.netguard.adapter.test.ngagent.session;

public class SessionContext {

	private static SessionManagerMock 
		managerInstance;

	public static void setManager(SessionManagerMock manager) {
		managerInstance = manager;
		
	}

	public static SessionManagerMock getManager() {
		return managerInstance;
	}
	

}
