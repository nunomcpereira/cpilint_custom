package com.nmp.cpilint.impl;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.issues.CsrfProtectionRequiredIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.SenderAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import dk.mwittrock.cpilint.rules.RuleBase;
import dk.mwittrock.cpilint.rules.XpathRulesUtil;
import net.sf.saxon.s9api.XdmNode;

final class CsrfProtectionRequiredWithExcludeRule extends RuleBase {
	private List<String> exclusionList; 
	private List<String> inclusionList;
	private boolean exclusionMode = true;
	
	public CsrfProtectionRequiredWithExcludeRule(List<String> exclusionList, List<String> inclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		this.inclusionList = inclusionList == null ? Collections.emptyList() : inclusionList;
		if (this.exclusionList.size() != 0) {
			exclusionMode = true;
			System.out.println(String.format("CSRF Protection Required following exclusion list [%s]",
					String.join(",", exclusionList)));
		}
		if (this.inclusionList.size() != 0) {
			exclusionMode = false;
			System.out.println(String.format("CSRF Protection Required following inclusion list [%s]",
					String.join(",", inclusionList)));
		}
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		IssueConsumer exclusionInclusionConsumer = new ExclusionInclusionIssueConsumer(exclusionList,inclusionList,tag.getId(),exclusionMode);
		String noCsrfChannelsXpath = model.xpathForSenderChannels(SenderAdapter.HTTPS, model.channelPredicateForNoCsrfProtection());
		Function<XdmNode, Issue> issueFunction = n -> new CsrfProtectionRequiredIssue(tag, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateSingleXpathAndConsumeIssues(iflowXml, noCsrfChannelsXpath, issueFunction, exclusionInclusionConsumer::consume);
	}

}
