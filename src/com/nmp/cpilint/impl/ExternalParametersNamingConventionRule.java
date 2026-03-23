package com.nmp.cpilint.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;

final class ExternalParametersNamingConventionRule extends RuleBase {

	private static final String PARAMETERS_PROP_FILE = "parameters.prop";

	private final Pattern namingPattern;

	ExternalParametersNamingConventionRule(Pattern namingPattern) {
		this.namingPattern = namingPattern;
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		Collection<ArtifactResource> externalParameters = iflow
				.getResourcesByType(ArtifactResourceType.EXTERNAL_PARAMETERS);
		for (ArtifactResource resource : externalParameters) {
			if (!PARAMETERS_PROP_FILE.equals(resource.getName())) {
				continue;
			}
			try {
				Properties props = new Properties();
				props.load(new ByteArrayInputStream(resource.getContents().readAllBytes()));
				for (String paramName : props.stringPropertyNames()) {
					if (!namingPattern.matcher(paramName).matches()) {
						consumer.consume(new ExternalParametersNamingConventionIssue(tag,
								String.format("External parameter name '%s' does not match naming convention '%s'",
										paramName, namingPattern.pattern())));
					}
				}
			} catch (IOException e) {
				System.err.println("Error reading parameters.prop in iflow " + tag + ": " + e.getLocalizedMessage());
			}
		}
	}

}
