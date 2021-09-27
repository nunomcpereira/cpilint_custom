package com.nmp.cpilint.impl;

import java.util.ArrayList;
import java.util.List;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.issues.IflowDescriptionRequiredIssue;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import dk.mwittrock.cpilint.rules.RuleBase;
import dk.mwittrock.cpilint.util.JarResourceUtil;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

final class DefaultNamesNotAllowedRule extends RuleBase {
	private class HandlerXQueryAnswerCallActivity {
		public String name;
		public String id;
		public String activityType;
		public String subActivityType;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\nName: [").append(name).append("] ");
			sb.append("Id: [").append(id).append("] ");
			sb.append("ActivityType: [").append(activityType).append("] ");
			sb.append("SubActivityType: [").append(subActivityType).append("]\n");
			return sb.toString();
		}
	}

	private class HandlerXQueryAnswerServiceTask {
		public String name;
		public String id;
		public String activityType;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\nName: [").append(name).append("] ");
			sb.append("Id: [").append(id).append("] ");
			sb.append("ActivityType: [").append(activityType).append("] ");
			return sb.toString();
		}
	}

	private static String[][] defaultNames = { {}, {} };

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		IflowArtifactTag tag = iflow.getTag();
		XdmValue callActivityNode = iflowXml.executeXquery(xqueryForDefaultNamesNotAllowedRuleCallActivity());
		List<HandlerXQueryAnswerCallActivity> result = mapNodeToCallActivityList(callActivityNode);
		processCallActivity(result, consumer, tag);

		XdmValue serviceTaskNode = iflowXml.executeXquery(xqueryForDefaultNamesNotAllowedRuleServiceTask());
		List<HandlerXQueryAnswerServiceTask> resultServiceTask = mapNodeToCallServiceTask(serviceTaskNode);
		processServiceTask(resultServiceTask, consumer, tag);
	}

	private List<HandlerXQueryAnswerServiceTask> mapNodeToCallServiceTask(XdmValue serviceTaskNode) {
		List<HandlerXQueryAnswerServiceTask> result = new ArrayList<>();
		HandlerXQueryAnswerServiceTask currentValue = new HandlerXQueryAnswerServiceTask();
		for (XdmItem value : serviceTaskNode) {
			if (currentValue.name == null) {
				currentValue.name = value.getStringValue();
			} else if (currentValue.id == null) {
				currentValue.id = value.getStringValue();
			} else if (currentValue.activityType == null) {
				currentValue.activityType = value.getStringValue();
				result.add(currentValue);
				currentValue = new HandlerXQueryAnswerServiceTask();
			}
		}
		return result;
	}

	private void processServiceTask(List<HandlerXQueryAnswerServiceTask> resultServiceTask, IssueConsumer consumer,
			IflowArtifactTag tag) {
		if (resultServiceTask == null) {
			return;
		}
		for (HandlerXQueryAnswerServiceTask value : resultServiceTask) {
			switch (value.activityType) {
			case "ExternalCall":
				if (value.name.matches("Request Reply \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("default name %s for request reply was found", value.name)));
				}
				break;
			default:
				System.err.println("Please check activity type for service task: " + value.activityType);
				break;
			}
		}
	}

	private void processCallActivity(List<HandlerXQueryAnswerCallActivity> result, IssueConsumer consumer,
			IflowArtifactTag tag) {
		if (result == null) {
			return;
		}
		for (HandlerXQueryAnswerCallActivity value : result) {
			switch (value.activityType) {
			case "Script":
				if ("GroovyScript".equals(value.subActivityType) && value.name.matches("Groovy Script \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for groovy script was found", value.name)));
				}
				break;
			case "Decoder":
				if (value.name.matches("Base64 Decoder \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for decoder was found", value.name)));
				}
				break;
			case "Enricher":
				if (value.name.matches("Content Modifier \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for Content Modifier was found", value.name)));
				} 
				break;
			case "ProcessCall":
				if (value.name.matches("Process Call \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for Process Call was found", value.name)));
				} 
				break;
			case "XmlValidator":
				if (value.name.matches("XML Validator \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for XML Validator was found", value.name)));
				} 
				break;
			case "EDI Validator":
				if (value.name.matches("EDI Validator \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for EDI Validator was found", value.name)));
				} 
				break;
			case "XMLtoEDIConverter":
				if (value.name.matches("XML to EDI Converter \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for XML to EDI Converter was found", value.name)));
				}
				break;
			case "ProcessCallElement":
				if (value.name.matches("Process Call Element \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for Process Call Element was found", value.name)));
				}
				break;
			case "Mapping":
				if (value.name.matches("Message Mapping \\d")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for Mapping was found", value.name)));
				}
				break;

			default:
				System.err.println("Please check activity type for call activity: " + value.activityType);
				break;
			}
		}
	}

	private List<HandlerXQueryAnswerCallActivity> mapNodeToCallActivityList(XdmValue callActivityNode) {
		List<HandlerXQueryAnswerCallActivity> result = new ArrayList<>();
		HandlerXQueryAnswerCallActivity currentValue = new HandlerXQueryAnswerCallActivity();
		for (XdmItem value : callActivityNode) {
			if (currentValue.name == null) {
				currentValue.name = value.getStringValue();
			} else if (currentValue.id == null) {
				currentValue.id = value.getStringValue();
			} else if (currentValue.activityType == null) {
				currentValue.activityType = value.getStringValue();
			} else if (currentValue.subActivityType == null) {
				currentValue.subActivityType = value.getStringValue();
				result.add(currentValue);
				currentValue = new HandlerXQueryAnswerCallActivity();
			}
		}
		return result;
	}

	public String xqueryForDefaultNamesNotAllowedRuleCallActivity() {
		return JarResourceUtil.loadXqueryResource("default-names-not-allowed-rule-callactivity.xquery");
	}

	public String xqueryForDefaultNamesNotAllowedRuleServiceTask() {
		return JarResourceUtil.loadXqueryResource("default-names-not-allowed-rule-servicetask.xquery");
	}

}
