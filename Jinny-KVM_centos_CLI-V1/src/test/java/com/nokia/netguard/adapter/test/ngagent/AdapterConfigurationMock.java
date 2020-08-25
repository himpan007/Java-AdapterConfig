package com.nokia.netguard.adapter.test.ngagent;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.nakina.adapter.base.agent.api.base.RequestFailure;

public class AdapterConfigurationMock {
	private Document doc;
	private String pathPrefix = "adapterConfiguration";

	public AdapterConfigurationMock(String adapterDefinitionDir) throws SAXException, IOException, ParserConfigurationException, RequestFailure {	
			File exeDir = new File(new File(System.getProperty("user.dir"), "target/classes/"), adapterDefinitionDir);
			Finder finder = new Finder("adapter-configuration.xml", FileVisitResult.TERMINATE);
			Files.walkFileTree(Paths.get(exeDir.getAbsolutePath()), finder);
			Vector<String> matchList = finder.getMatchList();
			if(matchList.size() == 0) 
				throw new RequestFailure("Can't find adapter-configuration.xml in " + exeDir.getAbsolutePath());
			else if(matchList.size() > 1) {
				StringBuilder builder = new StringBuilder();
				matchList.forEach(entry -> builder.append(entry).append(";"));
				throw new RequestFailure("Too many adapter-configuration.xml in " + exeDir.getAbsolutePath() + " " + builder.toString());
			}
			File xmlFile = new File(matchList.get(0));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
	}

	public String getStringValue(String field, String dafaultVal) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node;
		try {
			XPathExpression path = xPath.compile("/" + pathPrefix + "/" + field.replaceAll("\\.", "/") + "/text()");
			node = (Node) path.evaluate(doc, XPathConstants.NODE);
			if(node == null) {
				return dafaultVal;	
			}
		} catch (XPathExpressionException e) {
			return dafaultVal;
		}
		return node.getNodeValue();
	}

	public String getNeLineTermination() {
		String neLineTerm = this.getStringValue("ne.lineTermination", "\n");
		String lineTerminationChars = "";
		char[] var4 = neLineTerm.toCharArray();
		int var5 = var4.length;

		for (int var6 = 0; var6 < var5; ++var6) {
			char c = var4[var6];
			if (c != '\\') {
				if (c == 'n') {
					lineTerminationChars = lineTerminationChars + '\n';
				}

				if (c == 'r') {
					lineTerminationChars = lineTerminationChars + '\r';
				}
			}
		}

		return lineTerminationChars;
	}

	public Integer getIntegerValue(String field, Integer defaultVal) {
		String value = getStringValue(field, null);
		if (value == null) {
			return defaultVal;
		}
		return Integer.parseInt(value);
	}

	public Boolean getBooleanValue(String field, Boolean defaultVal) {
		String value = getStringValue(field, null);
		if (value == null) {
			return defaultVal;
		}
		return Boolean.parseBoolean(value);
	}
}
