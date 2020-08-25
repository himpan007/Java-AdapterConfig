package com.nokia.netguard.adapter.test.base;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.nakina.adapter.api.shared.util.Trace;

public class TestTracer extends TestWatcher {

	private NE ne;

	public TestTracer(NE ne) {
		this.ne = ne;
	}
	
	@Override
	protected void starting(Description description) {
		Trace.info(getClass(), "\n\n"
				+ "/-----\n"
				+ "|\n"
				+ "| TEST START\n"
				+ "| ne      "+ne+"\n"
				+ "| class   "+description.getClassName()+"\n"
				+ "| test    "+description.getMethodName()+"\n"
				+ "|\n"
				+ "\\-----\n");
	}

	@Override
	protected void succeeded(Description description) {
		Trace.info(getClass(), "\n\n"
				+ "/-----\n"
				+ "|\n"
				+ "| TEST SUCCESS\n"
				+ "| ne      "+ne+"\n"
				+ "| class   "+description.getClassName()+"\n"
				+ "| test    "+description.getMethodName()+"\n"
				+ "|\n"
				+ "\\-----\n");
	}
	
	@Override
	protected void failed(Throwable e, Description description) {
		String msg = e.getMessage().replaceAll("\n\r|\r\n|\r|\n", "\n|         ");
		Trace.info(getClass(), "\n\n"
				+ "/-----\n"
				+ "|\n"
				+ "| TEST FAILED\n"
				+ "| ne      "+ne+"\n"
				+ "| class   "+description.getClassName()+"\n"
				+ "| test    "+description.getMethodName()+"\n"
				+ "| error   "+e.getClass().getCanonicalName()+"\n"
				+ "| message "+msg+"\n"
				+ "|\n"
				+ "\\-----\n");
	}

}
