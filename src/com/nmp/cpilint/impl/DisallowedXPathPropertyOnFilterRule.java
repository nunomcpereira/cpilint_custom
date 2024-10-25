package com.nmp.cpilint.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.stream.Collectors;

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
import net.sf.saxon.s9api.XdmValue;

final class DisallowedXPathPropertyOnFilterRule extends RuleBase {

	private List<String> disallowedExpressionsList;

	private IflowArtifactTag currentTagBeingAnalysed = null;
	private Map<String, String> fileNameFileContentCache = new HashMap<>();

	public DisallowedXPathPropertyOnFilterRule(List<String> disallowedExpressionsList) {
		this.disallowedExpressionsList = disallowedExpressionsList == null ? Collections.emptyList() : disallowedExpressionsList;
		if (this.disallowedExpressionsList.size() > 0) {
			System.out.println(String.format("Disallowed Expressions Rule created with the following expression list [%s]",
					String.join(",", disallowedExpressionsList)));
		}
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		if (!tag.equals(currentTagBeingAnalysed)) {
			fileNameFileContentCache = new HashMap<>();
			currentTagBeingAnalysed = tag;
		}
		
		XdmValue result = iflow.getIflowXml().evaluateXpath(
				"//bpmn2:extensionElements[ifl:property[key='activityType' and value='Filter']]/ifl:property[key='wrapContent']/value/text()");
		String[] lines = result.toString().split("\n");
		for (String disallowedExpression : disallowedExpressionsList) {
			for (String line : lines) {
				if(Pattern.compile(disallowedExpression).matcher(line).find()) {
					consumer.consume(new DisallowedXPathPropertyOnFilterIssue(tag,
							String.format("Disallowed Filter Expression [%s] was found", disallowedExpression)));
				}
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
		fileNameFileContentCache.put(new String(filename), data);
		return data;
	}


}
