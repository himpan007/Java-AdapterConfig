package com.nokia.netguard.adapter.test.suite.offline.requests.gs.data.collecting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.csv.CSVRecord;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nokia.netguard.adapter.requests.gs.data.collecting.CSVReader;

public class CSVReaderTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void wrongPathTest() throws IOException {

		expectedException.expect(IOException.class);
		expectedException.expectMessage(matchesRegex(".*(No such file or directory|The system cannot find the path specified).*"));

		try (CSVReader reader = new CSVReader("notexistingpath", "notexistingfile")) {
			reader.read();
		}
	}

	@Test
	public void wrongFileNameTest() throws IOException {

		expectedException.expect(IOException.class);
		expectedException.expectMessage(matchesRegex(".*(No such file or directory|The system cannot find the file specified).*"));

		try (CSVReader reader = new CSVReader("target/classes", "ReadRules.csvv")) {
			reader.read();
		}
	}

	@Test
	public void correctFileTest() throws IOException {
		try (CSVReader reader = new CSVReader("target/classes", "ReadRules.csv")) {
			int lineCounter = 0;
			for (CSVRecord entity : reader.read()) {
				lineCounter++;
			}

			assertEquals(27, lineCounter);
		}
	}

	private Matcher<String> matchesRegex(final String regex) {

		return new TypeSafeMatcher<String>() {

			@Override
			protected boolean matchesSafely(final String item) {
				return item.matches(regex);
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("should match ").appendValue(regex);
			}
		};
	}
}
