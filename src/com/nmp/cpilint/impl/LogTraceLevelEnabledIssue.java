package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.ArtifactIssueBase;

public final class LogTraceLevelEnabledIssue extends ArtifactIssueBase {

	public LogTraceLevelEnabledIssue(IflowArtifactTag tag, String descriptionIssue) {
		super(tag, descriptionIssue);
	}

}
