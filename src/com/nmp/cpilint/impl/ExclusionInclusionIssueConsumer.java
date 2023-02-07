package com.nmp.cpilint.impl;

import java.util.List;

import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.issues.Issue;

public final class ExclusionInclusionIssueConsumer implements IssueConsumer {

	private int issuesConsumed = 0;
	private List<String> exclusionList;
	private List<String> inclusionList;
	private boolean exclusionMode = true;
	private String iflowId;

	public ExclusionInclusionIssueConsumer(List<String> exclusionList, List<String> inclusionList, String id,
			boolean exclusionMode) {
		this.iflowId = id;
		this.exclusionList = exclusionList;
		this.inclusionList = inclusionList;
		this.exclusionMode = exclusionMode;
	}

	@Override
	public void consume(Issue issue) {
		if (exclusionMode) {
			for (String line : exclusionList) {
				if (iflowId.matches(line)) {
					return;
				}
			}
		}
		if (!exclusionMode && inclusionList.size() > 0) {
			for (String line : inclusionList) {
				if (iflowId.matches(line)) {
					return;
				}
			}
		} else {
			System.out.println(issue.getMessage());
			issuesConsumed++;
		}
	}

	@Override
	public int issuesConsumed() {
		return issuesConsumed;
	}

}