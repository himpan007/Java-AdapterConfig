package com.nokia.netguard.adapter.requests.gs.data.collecting;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class CSVReader implements Closeable {
	private String csvPath;
	private BufferedReader br;
	private CSVParser parser;

	public CSVReader(String path, String fileName) {		
		csvPath = (new File(path, fileName)).getAbsolutePath();
	}

	public CSVParser read() throws IOException {
		// "@/" means that this is internal resource
		if (csvPath.contains("@" + File.separator)) {
			InputStream is = getClass().getResourceAsStream("/" + csvPath.substring(csvPath.indexOf('@') + 2));
			br = new BufferedReader(new InputStreamReader(is));
		} else {
			br = new BufferedReader(new FileReader(csvPath));
		}

		parser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreEmptyLines()
				.withCommentMarker('#').withDelimiter(','));
		return parser;
	}

	@Override
	public void close() throws IOException {
		if (parser != null) {
			parser.close();
		}

		if (br != null) {
			br.close();
		}
	}
}
