package com.nokia.netguard.adapter.cli.callback;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nakina.adapter.base.agent.api.base.RequestFailure;
import com.nokia.netguard.adapter.cli.ExecutionResult;

public class MatchOutput implements Callback {

	private static final Integer DEFAULT_PATTERN_FLAGS = CASE_INSENSITIVE | DOTALL;

	private String parameterName;

	private String pattern;

	private Integer flags;

	public static MatchOutput create(String parameterName) {
		return new MatchOutput(parameterName);
	}

	public MatchOutput(String parameterName) {
		this.parameterName = parameterName;
	}

	public MatchOutput setPattern(String pattern) {
		this.pattern = pattern;
		return this;
	}

	public void setFlags(Integer flags) {
		this.flags = flags;
	}

	@Override
	public void call(ExecutionResult executionResult) throws RequestFailure {
		String output = executionResult.getOutput();
		Pattern compiledPattern = Pattern.compile(pattern, (flags == null ? DEFAULT_PATTERN_FLAGS : flags));
		Matcher matcher = compiledPattern.matcher(output);
		if (!matcher.matches()) {
			throw new RequestFailure("Output does not match provided pattern. Parameter name: " +
					parameterName + ", pattern: " + pattern + ", output: " + output);
		}
	}
}
