package com.nmp.cpilint.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

final class LogTraceLevelEnabledRule extends RuleBase {

	private List<String> exclusionList;

	private IflowArtifactTag currentTagBeingAnalysed = null;
	private Map<String, String> fileNameFileContentCache = new HashMap<>();

	public LogTraceLevelEnabledRule(List<String> exclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		if (this.exclusionList.size() > 0) {
			System.out.println(String.format("Log Trace Level Enabled Rule created with the following exclusion list [%s]",
					String.join(",", exclusionList)));
		}
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		if (!tag.equals(currentTagBeingAnalysed)) {
			fileNameFileContentCache = new HashMap<>();
			currentTagBeingAnalysed = tag;
		}

		if(logBodyScriptExists(iflow) && !logTraceLevelEnabledExists(iflow)){
			if (exclusionList.contains(tag.getId())) {
				System.out.println(String.format("Iflow [%s] is excluded from exclusion list [%s]",
						tag.getId(), String.join(", ", exclusionList)));
				return;
			}
			
			consumer.consume(new LogTraceLevelEnabledIssue(tag,
					String.format("External parameter [%s] does not exist despite being required by the logBody.groovy script", "FER_LogTraceLevelEnabled")));
		}

	}

	public boolean logTraceLevelEnabledExists(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		Collection<ArtifactResource> externalParameters = iflow
				.getResourcesByType(ArtifactResourceType.EXTERNAL_PARAMETERS);
		for (ArtifactResource externalParam : externalParameters) {
			if (!"parameters.propdef".equals(externalParam.getName())) {
				continue;
			}
			try {
				String fileContentsStr = loadFileInStringFormat(externalParam.getName(), externalParam.getContents());

				Document contents = loadXMLFromString(fileContentsStr);
				NodeList nodeList = contents.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					String paramNameElement = "name";
					String externalParamName = "";
					if (item.getNodeName().equalsIgnoreCase("param_references")) {
						if (item.getFirstChild() == null) {
							continue;
						}
						paramNameElement = "param_key";
						NamedNodeMap attributes = item.getChildNodes().item(0).getAttributes();
						for (int y = 0; y < attributes.getLength(); y++) {
							if (paramNameElement.equalsIgnoreCase(attributes.item(y).getNodeName())) {
								externalParamName = attributes.item(y).getTextContent();
								break;
							}
						}
					} else {
						NodeList parameterFields = item.getChildNodes();

						for (int y = 0; y < parameterFields.getLength(); y++) {
							if (paramNameElement.equalsIgnoreCase(parameterFields.item(y).getNodeName())) {
								externalParamName = parameterFields.item(y).getTextContent();
								break;
							}
						}
					}
					if ("".equals(externalParamName)) {
						if (item.getNodeName().equalsIgnoreCase("param_references") && item.getFirstChild() != null) {
							System.err.println("Parameter not found for " + item + " on iflow" + tag);
						}
						continue;
					}
					if("FER_LogTraceLevelEnabled".equals(externalParamName)){
						return true;
					}
				}
			} catch (Exception e) {
				System.err.println("Exception: [" + e.getLocalizedMessage() + "]");
			}
		}
		return false;


	}	

	public boolean logBodyScriptExists(IflowArtifact iflow) {
		IflowXml iflowXmlInputStream = iflow.getIflowXml();
		String iflowXml = new BufferedReader(new InputStreamReader(iflowXmlInputStream.getRawDocument())).lines().collect(Collectors.joining("\n"));

		if(iflowXml.contains("<value>logBody.groovy</value>")) {
			return true;
		}
		return false;

	}


	private String loadFileInStringFormat(String filename, InputStream contents) throws IOException {
		String resultCache = fileNameFileContentCache.get(filename);
		if (resultCache != null) {
			return resultCache;
		}
		ByteArrayInputStream arrayInputStream = null;

		arrayInputStream = new ByteArrayInputStream(contents.readAllBytes());

		Scanner scanner = new Scanner(arrayInputStream);
		scanner.useDelimiter("\\Z");// To read all scanner content in one String
		String data = "";
		if (scanner.hasNext())
			data = scanner.next();
		scanner.close();
		fileNameFileContentCache.put(new String(filename), data);
		return data;
	}


	private static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

}
