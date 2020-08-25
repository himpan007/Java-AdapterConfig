package com.nokia.netguard.adapter.test.ngagent.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nakina.adapter.api.shared.util.Trace;
import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.test.ngagent.session.SessionChannel;

public class Command {

	protected static final int NULL_TIMEOUT = -1;

	protected String m_result = null;
	protected String m_commandName = null;
	protected String m_commandString = null;
	protected int m_timeout = NULL_TIMEOUT;
	protected String m_regex = ".*";
	protected String m_matchedPattern = null;

	protected SessionChannel m_channel = null;
	
	protected PrintStream ps;
	protected InputStream dataIn;
	protected OutputStream dataOut;
	
	public Command(SessionChannel channel, String name, String theCommandString, String aRegex, int timeout) throws IOException {
		m_commandName = name;
		m_commandString = theCommandString;
		m_regex = "" + aRegex;
		m_timeout = timeout;
		m_channel = channel;
		dataIn = channel.getInputStream();
		dataOut = channel.getOutputStream();
	}

	public void addPattern(String pattern) {
		this.m_regex = this.m_regex + "|" + pattern;
	}
	
	protected boolean foundMatch(String output) throws RequestFailure {
		try {
			output = output.replace("\n", " ").replace("\r", "");

			Pattern p = Pattern.compile(m_regex, Pattern.MULTILINE);
			Matcher m = p.matcher(output);

			if (m.find()) {
				m_result = m.group();
				return true;
			}

		} catch (Exception e) {
			throw new RequestFailure("Error while finding match: " + e);
		}
		return false;
	}

	private String executeCommand() throws RequestFailure {
		Trace.info(getClass(), "<< SEND ["+m_commandString+"]");
		StringBuilder response = new StringBuilder();

		byte[] buffer = new byte[1024];

		try {			
			if (ps == null)				
				ps = new PrintStream(dataOut, true);
			
			ps.print(m_commandString);
			
			String line = "";
			long start = System.currentTimeMillis();
			long end = start;

			while (m_timeout > end - start) {
				end = System.currentTimeMillis();

				while (dataIn.available() > 0) {
					int j = dataIn.read(buffer, 0, 1024);
					if (j < 0) {
						break;
					}
					line = new String(buffer, 0, j);
					response.append(line);
				}

				if (foundMatch(response.toString())) {
					Trace.info(getClass(), ">> RECV ["+response.toString()+"], REGEX ["+m_regex+"]");
					return response.toString();
				}
			}

			Trace.info(getClass(), ">> RECV ["+response.toString()+"], REGEX ["+m_regex+"]");
			throw new RequestFailure("Command timeout! Response: "+response.toString());
		} catch (Exception e) {
			RequestFailure exception = new RequestFailure(
					"Error while sending command: " + m_commandString + ". Cause: " + e.getMessage());
			exception.setStackTrace(e.getStackTrace());
			throw exception;
		}
	}

	public String send() throws RequestFailure {
		if (m_channel == null) {
			throw new RequestFailure("SessionManager is not initialized!");
		}
		long commandStart = System.currentTimeMillis();

		try {
			m_result = executeCommand();
		} finally {
			Trace.info(getClass(), "Command: "
					+ (m_commandString != null ? m_commandString.replace("\n", "").replaceAll("'r", "")
							: "COMMAND IS NULL ")
					+ " timeout: " + m_timeout + " executed time in millis: "
					+ (System.currentTimeMillis() - commandStart));
		}

		if (m_result == null) {
			throw new RequestFailure("Error occurred while trying to send command: " + m_commandString);
		}

		return m_result;
	}
}
