package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.ArtifactIssueBase;

public final class NameNotDefaultRequiredIssue extends ArtifactIssueBase {

	public NameNotDefaultRequiredIssue(IflowArtifactTag tag, String descriptionIssue) {
		super(tag, descriptionIssue);
	}

}
