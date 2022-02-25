package com.nmp.cpilint.impl;

import java.util.Collections;
import java.util.List;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

final class ResponseHeadersAllRule extends RuleBase {

	private List<String> exclusionList;
	private List<String> inclusionList;
	private boolean exclusionMode = true;

	public ResponseHeadersAllRule(List<String> exclusionList, List<String> inclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
		this.inclusionList = inclusionList == null ? Collections.emptyList() : inclusionList;
		if (this.exclusionList.size() != 0) {
			exclusionMode = true;
			System.out.println(String.format("All response headers created with the following exclusion list [%s]",
					String.join(",", exclusionList)));
		}
		if (this.inclusionList.size() != 0) {
			exclusionMode = false;
			System.out.println(String.format("All response headers created with the following inclusion list [%s]",
					String.join(",", inclusionList)));
		}
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		XdmValue result = iflow.getIflowXml().evaluateXpath(
				"//bpmn2:messageFlow/bpmn2:extensionElements/ifl:property[key='allowedResponseHeaders']/value/text()");
		XdmSequenceIterator<XdmItem> it = result.iterator();
		while (it.hasNext()) {
			XdmItem xdmItem = (XdmItem) it.next();
			String itemValue = xdmItem.getStringValue();
			String[] values = itemValue.split("|");
			if(inclusionList.size()>0)
			{
				for(String line:inclusionList)
				{
					for(String value:values)
					{
						if(!value.matches(line))
						{
							consumer.consume(new ResponseHeadersIssue(tag,
									String.format("Response headers [%s] is not allowed for iflow [%s]", itemValue, tag.getId())));
						}
					}
				}
			}
			else if(exclusionList.size()>0)
			{
				for(String line:exclusionList)
				{
					for(String value:values)
					{
						if(value.matches(line))
						{
							consumer.consume(new ResponseHeadersIssue(tag,
									String.format("Response headers [%s] is not allowed for iflow [%s]", itemValue, tag.getId())));
						}
					}
				}
			}
		}
	}

}
