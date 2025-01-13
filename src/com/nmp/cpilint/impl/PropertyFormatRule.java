package com.nmp.cpilint.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;
import net.sf.saxon.s9api.XdmValue;
import dk.mwittrock.cpilint.SharedMemory;

public final class PropertyFormatRule extends RuleBase {

    private final List<Map<String, String>> propertyFormatRules;
    private IflowArtifactTag currentTagBeingAnalysed = null;
    private Map<String, String> fileNameFileContentCache = new HashMap<>();

    public PropertyFormatRule(List<Map<String, String>> rules) {
        // Initialize the rules list
        this.propertyFormatRules = rules == null ? Collections.emptyList() : rules;

        if (!this.propertyFormatRules.isEmpty()) {
            // Log the rule initialization (customize to match your requirements)
            String ruleDetails = this.propertyFormatRules.stream()
                .map(rule -> String.format(
                    "{activationDate: %s, ruleExpression: %s}",
                    rule.get("activationDate"),
                    rule.get("ruleExpression")
                ))
                .collect(Collectors.joining(", "));

            System.out.println(String.format("Property Format Rule created with the following rules: [%s]", ruleDetails));
            
        }
    }

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		if (!tag.equals(currentTagBeingAnalysed)) {
			fileNameFileContentCache = new HashMap<>();
			currentTagBeingAnalysed = tag;
		}
		HashMap<String, String> creationDates = SharedMemory.creationDates;
		Collection<ArtifactResource> resources = iflow.getResourcesByType(ArtifactResourceType.GROOVY_SCRIPT);
		String propertyTablesString = iflow.getIflowXml().evaluateXpath(
				"//ifl:property[key='propertyTable']/value/text()").toString();
        
        propertyTablesString = propertyTablesString.replace("&lt;", "<").replace("&gt;", ">");

        // Regular expression to extract the value between id='Name' and </cell>
        String regex = "<cell id='Name'>(.*?)</cell>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(propertyTablesString);

     // ArrayList to store extracted values
        ArrayList<String> extractedProperties = new ArrayList<>();

        // Add all matches to the ArrayList
        while (matcher.find()) {
        	extractedProperties.add(matcher.group(1));
        }
        
		for (Map<String, String> rule : propertyFormatRules) {
	        String ruleExpression = rule.get("ruleExpression");
	        String activationDate = rule.get("activationDate");
	        List<String> exceptions = Arrays.asList(rule.get("exceptions").split(","));
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        LocalDate activation = LocalDate.parse(activationDate, formatter);
	        
	        creationDates.forEach((key, value) -> {
	        	long creationDateLong = Long.parseLong(value);
	        	LocalDate creationDate = Instant.ofEpochMilli(creationDateLong).atZone(ZoneId.systemDefault()).toLocalDate();
		        if(key.equals(tag.getName()) && activation.isBefore(creationDate)) {
		        	//check in iflow steps
			        for(String property: extractedProperties) {
			        	if(!Pattern.compile(ruleExpression).matcher(property).find() && !exceptions.contains(property)) {
							consumer.consume(new PropertyFormatIssue(tag,
								String.format("Property [%s] does not follow the format rule [%s]", property, ruleExpression)));
						}
			        }
				    //check in scripts
					if (resources != null) {
						for (ArtifactResource resource : resources) {
							try {
								String groovyContentsStr = loadFileInStringFormat(resource.getName(), resource.getContents());
								// Compile the regex
								String groovyRegex = "\\.setProperty\\(\"([^\"]+)\", ";
						        Pattern groovPattern = Pattern.compile(groovyRegex);
						        Matcher groovyMatcher = groovPattern.matcher(groovyContentsStr);
						        
						        // List to store all matches
						        List<String> regexMatches = new ArrayList<>();
						        
						        // Find all matches
						        while (groovyMatcher.find()) {
						        	regexMatches.add(groovyMatcher.group(1));
						        }
						        
						        // Print the matches
						        for (String match : regexMatches) {
						            if(!Pattern.compile(ruleExpression).matcher(match).find() && !exceptions.contains(match)) {
						            	consumer.consume(new PropertyFormatIssue(tag,
												String.format("Property [%s] does not follow the format rule [%s] in groovy script", match, ruleExpression)));
									}
						        }
								
							} catch (Exception e) {
								System.err.println("Exception: [" + e.getLocalizedMessage() + "]");
							}
						}
					}
		        }
	        });
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
