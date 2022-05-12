package com.nmp.cpilint.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;

final class UnusedParametersRule extends RuleBase {

	private List<String> exclusionList;

	private IflowArtifactTag currentTagBeingAnalysed = null;
	private Map<String, String> fileNameFileContentCache = new HashMap<>();

	public UnusedParametersRule(List<String> exclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		if (this.exclusionList.size() > 0) {
			System.out.println(String.format("Unused rule created with the following exclusion list [%s]",
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
					if (exclusionList.contains(externalParamName)) {
						System.out.println(String.format("Parameter [%s] is globally excluded from exclusion list [%s]",
								externalParamName, String.join(", ", exclusionList)));
						continue;
					}
					if (exclusionList.contains(tag.getId() + ":" + externalParamName)) {
						System.out.println(String.format("Parameter [%s] is iflow excluded from exclusion list [%s]",
								externalParamName, String.join(", ", exclusionList)));
						continue;
					}

					boolean used = isExternalParameterBeingUsed(externalParamName, iflow);
					if (!used) {
						consumer.consume(new UnusedParameterIssue(tag,
								String.format("External parameter [%s] not being used", externalParamName)));
					}

				}
			} catch (Exception e) {
				System.err.println("Exception: [" + e.getLocalizedMessage() + "]");
			}
		}
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
		fileNameFileContentCache.put(filename, data);
		return data;
	}

	private boolean isExternalParameterBeingUsed(String externalParamName, IflowArtifact iflow) throws IOException {
		String patternString = "\\b" + externalParamName + "\\b";
		Pattern pattern = Pattern.compile(patternString);
		for (ArtifactResourceType entry : ArtifactResourceType.values()) {
			if (entry.equals(ArtifactResourceType.EXTERNAL_PARAMETERS) || entry.equals(ArtifactResourceType.EDMX)
					|| entry.equals(ArtifactResourceType.WSDL)) {
				continue;
			}
			Collection<ArtifactResource> resources = iflow.getResourcesByType(entry);
			for (ArtifactResource resource : resources) {
				
				String fileInStrFormat = loadFileInStringFormat(resource.getName(), resource.getContents());
				if (entry.equals(ArtifactResourceType.IFLOW)) {
					if (fileInStrFormat.contains("{{" + externalParamName + "}}")) {
						return true;
					}
					continue;
				}
				Matcher matcher = pattern.matcher(fileInStrFormat);
				if (matcher.find()) {
					return true;
				}

			}
		}
		return false;
	}

	private static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

}
