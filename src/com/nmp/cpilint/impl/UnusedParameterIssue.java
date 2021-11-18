package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.ArtifactIssueBase;

public final class UnusedParameterIssue extends ArtifactIssueBase {

	public UnusedParameterIssue(IflowArtifactTag tag, String descriptionIssue) {
		super(tag, descriptionIssue);
	}

}
