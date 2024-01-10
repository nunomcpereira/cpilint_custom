package com.nmp.cpilint.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.rules.RuleBase;
import dk.mwittrock.cpilint.util.JarResourceUtil;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

final class DefaultNamesNotAllowedRule extends RuleBase {

	private List<String> exclusionList;

	public DefaultNamesNotAllowedRule(List<String> exclusionList) {
		this.exclusionList = exclusionList == null ? Collections.emptyList() : exclusionList;
	}

	private static String[][] simpleActivityTypeRules = { { "Enricher", "Content Modifier \\d", "Content Modifier" },
			{ "ProcessCall", "Process Call \\d", "Process Call" },
			{ "ProcessCallElement", "Process Call \\d", "Process Call Element" },
			{ "XmlValidator", "XML Validator \\d", "XML Validator" },
			{ "EDIValidator", "EDI Validator \\d", "EDI Validator" },
			{ "XMLtoEDIConverter", "XML to EDI Converter \\d", "XML to EDI Converter" },
			{ "ProcessCallElement", "Process Call Element \\d", "Process Call Element" },
			{ "Mapping", "(Message Mapping|Operation Mapping|XSLT Mapping) \\d", "Message Mapping" },
			{ "XmlToCsvConverter", "XML to CSV Converter \\d", "XML To CSV Converter" },
			{ "JsonToXmlConverter", "JSON to XML Converter \\d", "JSON to XML Converter" },
			{ "CsvToXmlConverter", "CSV to XML Converter \\d", "CSV to XML Converter" },
			{ "XmlToJsonConverter", "XML to JSON Converter \\d", "XmlToJsonConverter" },
			{ "DBstorage", "(Delete|Get|Select|Write) \\d", "DB storage" }, { "Persist", "Persist \\d", "Persist" },
			{ "Encoder", "(MIME Multipart Encoder|Base64 Encoder|ZIP Compression|GZIP Compression) \\d", "Encoder" },
			{ "Decoder", "(GZIP Decompression|ZIP Decompression|Base64 Decoder|MIME Multipart Decoder) \\d",
					"Decoder" },
			{ "Filter", "Filter \\d", "Filter" },
			{ "VerifySign", "PKCS7 Signature Verifier \\d", "PKCS7 Signature Verifier" },
			{ "EDIExtractor", "EDI Extractor \\d", "EDI Extractor" },
			{ "EDItoXMLConverter", "EDI to XML Converter \\d", "EDI to XML Converter" },
			{ "SimpleSignMessage", "Simple Signer \\d", "Simple Signer" },
			{ "Encrypt", "PKCS7Encryptor \\d", "PKCS7Encryptor" }, { "PgpEncrypt", "PGPEncryptor \\d", "PgpEncrypt" },
			{ "Decrypt", "PKCS7Decryptor \\d", "Decrypt" }, { "PgpDecrypt", "PGPDecryptor \\d", "PgpDecrypt" },
			{ "Aggregator", "Aggregator \\d", "Aggregator" }, { "Gather", "Gather \\d", "Gather" },
			{ "VerifySign", "PKCS7 Signature Verifier \\d", "PKCS7 Signature Verifier" },
			{ "SimpleSignMessage", "Simple Signer \\d", "Simple Signer" },
			{ "XMLDigitalSignMessage", "XML Signer \\d", "XML Signer" },
			{ "SignMessage", "PKCS7 Signer \\d", "PKCS7 Signer" },
			{ "XmlModifier", "XML Modifier \\d", "XML Modifier" },
			{ "MessageDigest", "Message Digest \\d", "Message Digest" },
			{ "XMLDigitalVerifySign", "XML Signature Verifier \\d", "XML Signature Verifier" },
			{ "Variables", "Write Variables \\d", "Variables" },
			{ "Splitter",
					"(EDI Splitter|Zip Splitter|Iterating Splitter|General Splitter|IDoc Splitter|PKCS#7/CMS Splitter|Tar Splitter) \\d",
					"Splitter" }

	};

	private static String[][] simpleServiceTypeRules = { { "Send", "Send \\d", "Send" },
			{ "contentEnricherWithLookup", "Content Enricher \\d", "Content Enricher" },
			{ "PollEnrich", "Poll Enrich \\d", "Poll Enrich" },
			{ "ExternalCall", "Request Reply \\d", "Request Reply" },
			{ "ExclusiveGateway", "Router \\d", "Router" } };

	private class HandlerXQueryAnswerProcesses {
		public String name;
		public String id;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\nName: [").append(name).append("] ");
			sb.append("Id: [").append(id).append("]\n ");
			return sb.toString();
		}
	}

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

		XdmValue processesNode = iflowXml.executeXquery(xqueryForDefaultNamesNotAllowedRuleProcess());
		List<HandlerXQueryAnswerProcesses> resultProcesses = mapNodeToProcesses(processesNode);
		processProcesses(resultProcesses, consumer, tag);

		Collection<ArtifactResource> resources = iflow.getResourcesByType(ArtifactResourceType.GROOVY_SCRIPT);
		if (resources != null) {
			for (ArtifactResource resource : resources) {
				String name = resource.getName() != null ? resource.getName() : "";
				if (name.toLowerCase().matches("script\\d.groovy")) {
					consumer.consume(
							new NameNotDefaultRequiredIssue(tag, String.format("Default filename %s for %s was found",
									name, ArtifactResourceType.GROOVY_SCRIPT.getName())));
				}
			}
		}
		resources = iflow.getResourcesByType(ArtifactResourceType.XSLT_MAPPING);
		if (resources != null) {
			for (ArtifactResource resource : resources) {
				String name = resource.getName() != null ? resource.getName() : "";
				if (name.toLowerCase().matches("xsltmapping\\d.xsl")||name.toLowerCase().matches("xsltmapping\\d.xslt")) {
					consumer.consume(
							new NameNotDefaultRequiredIssue(tag, String.format("Default filename %s for %s was found",
									name, ArtifactResourceType.XSLT_MAPPING.getName())));
				}
			}
		}
	}

	private void processProcesses(List<HandlerXQueryAnswerProcesses> resultProcesses, IssueConsumer consumer,
			IflowArtifactTag tag) {
		if (resultProcesses == null) {
			return;
		}
		for (HandlerXQueryAnswerProcesses value : resultProcesses) {
			boolean handled = false;

			if (value.id == null || isExcluded(value.id)) {
				handled = true;
				continue;
			}
			if (value.id.startsWith("SubProcess")) {
				if (value.name.matches("Exception Subprocess \\d") && !isExcluded("Exception Subprocess")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for %s was found", value.name, "Exception")));
				}
				handled = true;
			}
			if (value.id.startsWith("Process")) {
				if (value.name.matches("(Integration Process \\d)")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag,
							String.format("Default name %s for %s was found", value.name, "Integration Process")));
				} else if (value.name.matches("(Local Integration Process|Local Integration Process \\d)")) {
					consumer.consume(new NameNotDefaultRequiredIssue(tag, String
							.format("Default name %s for %s was found", value.name, "Local Integration Process")));
				}
				handled = true;
			}
			if (!handled) {
				System.err.println("Please check rules for: [" + value.id + "] with name [" + value.name
						+ "] on iflow [" + tag + "]");

			}

		}
	}

	private boolean isExcluded(String id) {
		if (exclusionList.size() == 0) {
			return false;
		}
		boolean result = exclusionList.contains(id);
		if (result) {
			System.out.println(String.format("Element [%s] is being ignored by the rules", id));
		}
		return result;
	}

	private List<HandlerXQueryAnswerProcesses> mapNodeToProcesses(XdmValue processesNode) {
		List<HandlerXQueryAnswerProcesses> result = new ArrayList<>();
		HandlerXQueryAnswerProcesses currentValue = new HandlerXQueryAnswerProcesses();
		for (XdmItem value : processesNode) {
			if (currentValue.name == null) {
				currentValue.name = value.getStringValue();
			} else if (currentValue.id == null) {
				currentValue.id = value.getStringValue();
				result.add(currentValue);
				currentValue = new HandlerXQueryAnswerProcesses();
			}
		}
		return result;
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
			boolean handled = false;
			for (String[] serviceTypeObj : simpleServiceTypeRules) {
				if (value.activityType == null || serviceTypeObj.length < 3 || isExcluded(value.activityType)) {
					handled = true;
					continue;
				}
				if (value.activityType.equalsIgnoreCase(serviceTypeObj[0])) {
					if (value.name.matches(serviceTypeObj[1])) {
						consumer.consume(new NameNotDefaultRequiredIssue(tag,
								String.format("Default name %s for %s was found", value.name, serviceTypeObj[2])));
					}
					handled = true;
					break;
				}
			}
			if (!handled) {
				System.err.println("Please check activity type for service task: [" + value.activityType
						+ "] with name [" + value.name + "]");

			}

		}
	}

	private void processCallActivity(List<HandlerXQueryAnswerCallActivity> result, IssueConsumer consumer,
			IflowArtifactTag tag) {
		if (result == null) {
			return;
		}
		for (HandlerXQueryAnswerCallActivity value : result) {
			boolean handled = false;
			for (String[] activityTypeObj : simpleActivityTypeRules) {
				if (value.activityType == null || activityTypeObj.length < 3 || isExcluded(value.activityType)) {
					handled = true;
					continue;
				}
				if (value.activityType.equalsIgnoreCase(activityTypeObj[0])) {
					if (value.name.matches(activityTypeObj[1])) {
						consumer.consume(new NameNotDefaultRequiredIssue(tag,
								String.format("Default name %s for %s was found", value.name, activityTypeObj[2])));
					}
					handled = true;
					break;
				}
			}
			if (!handled) {
				switch (value.activityType) {
				case "Script":
					if ("GroovyScript".equals(value.subActivityType) && value.name.matches("Groovy Script \\d")) {
						consumer.consume(new NameNotDefaultRequiredIssue(tag,
								String.format("Default name %s for groovy script was found", value.name)));
					}
					break;
				default:
					System.err.println("Please check activity type for call activity: [" + value.activityType
							+ "] with name [" + value.name + "]");
					break;
				}
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

	public String xqueryForDefaultNamesNotAllowedRuleProcess() {
		return JarResourceUtil.loadXqueryResource("default-names-not-allowed-rule-process.xquery");
	}

}
