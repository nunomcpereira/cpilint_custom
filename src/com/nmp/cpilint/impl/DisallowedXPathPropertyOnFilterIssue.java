package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.ArtifactIssueBase;

public final class DisallowedXPathPropertyOnFilterIssue extends ArtifactIssueBase {

	public DisallowedXPathPropertyOnFilterIssue(IflowArtifactTag tag, String descriptionIssue) {
		super(tag, descriptionIssue);
	}

}
