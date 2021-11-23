package com.nmp.cpilint.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;

final class UnusedParametersRule2 extends RuleBase {

	private List<String> exclusionList;

	private IflowArtifactTag currentTagBeingAnalysed = null;
	private Map<String, String> fileNameFileContentCache = new HashMap<>();

	public UnusedParametersRule2(List<String> exclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		try {
			List<String> paramNamesToCheck = new ArrayList<>();
			IflowArtifactTag tag = iflow.getTag();
			if (!tag.equals(currentTagBeingAnalysed)) {
				fileNameFileContentCache = new HashMap<>();
				currentTagBeingAnalysed = tag;
			} else {
				System.err.println("Reused cache");
			}
			Collection<ArtifactResource> externalParameters = iflow
					.getResourcesByType(ArtifactResourceType.EXTERNAL_PARAMETERS);
			for (ArtifactResource externalParam : externalParameters) {
				if (!"parameters.propdef".equals(externalParam.getName())) {
					continue;
				}

				String fileContentsStr = loadFileInStringFormat(externalParam.getName(), externalParam.getContents());

				Document contents = loadXMLFromString(fileContentsStr);
				NodeList nodeList = contents.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					NodeList parameterFields = nodeList.item(i).getChildNodes();
					String externalParamName = "";
					for (int y = 0; y < parameterFields.getLength(); y++) {
						if ("name".equalsIgnoreCase(parameterFields.item(y).getNodeName())) {
							externalParamName = parameterFields.item(y).getTextContent();
							break;
						}
					}
					if (!"".equals(externalParamName) && !exclusionList.contains(externalParamName)) {
						paramNamesToCheck.add(externalParamName);

					}
				}

			}
			List<String> unUsed = isExternalParametersBeingUsed(paramNamesToCheck, iflow);
			if (unUsed != null && unUsed.size() > 0) {
				unUsed.forEach(externalParamName -> consumer.consume(new UnusedParameterIssue(tag,
						String.format("External parameter %s not being used", externalParamName))));

			}
		} catch (Exception e) {
			System.err.println("Exception: [" + e.getLocalizedMessage() + "]");
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

	private List<String> isExternalParametersBeingUsed(List<String> paramNamesToCheck, IflowArtifact iflow)
			throws IOException {
		if (paramNamesToCheck == null || paramNamesToCheck.size() == 0) {
			return Collections.emptyList();
		}
		List<String> unusedParameters = new ArrayList<>(paramNamesToCheck);
		StringBuilder regex = new StringBuilder();
		StringBuilder regexIflow = new StringBuilder("");
		for (String paramName : paramNamesToCheck) {
			regex.append("\\b").append(paramName).append("\\b").append("|");
			regexIflow.append("\\b").append("\\{\\{").append(paramName).append("\\}\\}").append("\\b").append("|");
		}
		String patternString = null;
		String patternStringIflow = null;
		if (regex.charAt(regex.length() - 1) == '|') {
			patternString = ".*(" + regex.substring(0, regex.length() - 1) + ").*";
			patternStringIflow = ".*(" + regexIflow.substring(0, regexIflow.length() - 1) + ").*";
		}

		Pattern pattern = Pattern.compile(patternString);
		Pattern patternIflow = Pattern.compile(patternStringIflow);
		Pattern patternToUse = null;
		for (ArtifactResourceType entry : ArtifactResourceType.values()) {
			if (entry.equals(ArtifactResourceType.EXTERNAL_PARAMETERS)) {
				continue;
			}
			Collection<ArtifactResource> resources = iflow.getResourcesByType(entry);
			for (ArtifactResource resource : resources) {
				String fileInStrFormat = loadFileInStringFormat(resource.getName(), resource.getContents());

				if (entry.equals(ArtifactResourceType.IFLOW)) {
					patternToUse = patternIflow;
				} else {
					patternToUse = pattern;
				}
				Matcher matcher = patternToUse.matcher(fileInStrFormat);
				if (matcher.find()) {
					for (int i = 0; i < matcher.groupCount(); i++) {
						Iterator<String> it = unusedParameters.iterator();
						while (it.hasNext()) {
							String next = it.next();
							if (matcher.group(i).contains(next)) {
								System.err.println("Param " + next + " being used on file " + resource.getName()
										+ " sequence found:" + matcher.group(i));
								it.remove();
							}
						}
					}
				}
			}
		}
		return unusedParameters;
	}

	private static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

}
