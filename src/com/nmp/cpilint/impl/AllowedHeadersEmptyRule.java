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
	private List<String> inclusionList;
	private boolean exclusionMode = true;

	public AllowedHeadersEmptyRule(List<String> exclusionList, List<String> inclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		this.inclusionList = inclusionList == null ? Collections.emptyList() : inclusionList;
		if (this.exclusionList.size() != 0) {
			exclusionMode = true;
			System.out.println(String.format("Allowed headers empty created with the following exclusion list [%s]",
					String.join(",", exclusionList)));
		}
		if (this.inclusionList.size() != 0) {
			exclusionMode = false;
			System.out.println(String.format("Allowed headers empty created with the following inclusion list [%s]",
					String.join(",", inclusionList)));
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
			if (exclusionMode) {
				isExcluded = false;
				for (String line : exclusionList) {
					if (tag.getId().matches(line)) {
						isExcluded = true;
					}
				}
			} else {
				isExcluded = true;
				for (String line : inclusionList) {
					if (tag.getId().matches(line)) {
						isExcluded = false;
					}
				}
			}
			if (!isExcluded) {
				consumer.consume(new AllowedHeadersEmptyIssue(tag,
						String.format("Allowed headers empty is not allowed for iflow [%s]", tag.getId())));
			}
		}
	}

}
