package com.nmp.cpilint.impl;

import java.util.Collections;
import java.util.List;

import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;
import net.sf.saxon.s9api.XdmValue;

final class AllowedHeadersEmptyRule extends RuleBase {

	private List<String> exclusionList;

	public AllowedHeadersEmptyRule(List<String> exclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		if (this.exclusionList.size() == 0) {
			System.out.println(String.format("Allowed headers empty created with the following exclusion list [%s]",
					String.join(",", exclusionList)));
		}
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		XdmValue result = iflow.getIflowXml().evaluateXpath(
				"//bpmn2:collaboration/bpmn2:extensionElements/ifl:property[key='allowedHeaderList']/value/text()");
		System.err.println(result);
		if (result.isEmpty()) {
			boolean isExcluded = false;
			for (String line : exclusionList) {
				if (tag.getId().matches(line)) {
					isExcluded = true;
				}
			}
			if (!isExcluded) {
				consumer.consume(new AllowedHeadersEmptyIssue(tag,
						String.format("Allowed headers empty is not allowed for iflow [%s]", tag.getId())));
			}
		}
	}

}
