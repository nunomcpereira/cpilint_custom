package com.nmp.cpilint.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import dk.mwittrock.cpilint.IflowXmlError;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.rules.RuleBase;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

final class UndeclaredContentModifierDatatypeRule extends RuleBase {

	@Override
	public void inspect(IflowArtifact iflow) {
		XdmValue headers = iflow.getIflowXml()
				.evaluateXpath("//bpmn2:callActivity/bpmn2:extensionElements/ifl:property[key='headerTable']/value");
		XdmValue properties = iflow.getIflowXml()
				.evaluateXpath("//bpmn2:callActivity/bpmn2:extensionElements/ifl:property[key='propertyTable']/value");
		XdmValue headersnewsyntax = iflow.getIflowXml().evaluateXpath(
				"//bpmn2:callActivity/bpmn2:extensionElements/ifl:property[starts-with(key,'HEADER_')]/value");
		IflowArtifactTag tag = iflow.getTag();
		validateValues(headers, tag);
		validateValues(properties, tag);
		validateValues(headersnewsyntax, tag);

	}

	private void validateValues(XdmValue values, IflowArtifactTag tag) {
		if (values != null && !values.isEmpty()) {
			XdmSequenceIterator<XdmItem> it = values.iterator();
			while (it.hasNext()) {
				XdmItem xdmItem = (XdmItem) it.next();
				String xmlValue = xdmItem.getUnderlyingValue().getStringValue();
				if (xmlValue == null) {
					continue;
				}
				if (xmlValue.equals(
						"<row><cell id='Action'>Create</cell><cell id='Type'>expression</cell><cell id='Value'>exported=true&status=approved&bill-to-address[country][code][in]=${property.FER_CountryCode}&invoice-date[gt_or_eq]=${property.FER_InvoiceStartDate}</cell><cell id='Default'></cell><cell id='Name'>loop_query</cell><cell id='Datatype'>java.lang.String</cell></row><row><cell id='Action'>Create</cell><cell id='Type'>expression</cell><cell id='Value'>&invoice-date[lt_or_eq]=${property.FER_InvoiceEndDate}&tags[name][not_eq]=SOVOS_SUCCESS</cell><cell id='Default'></cell><cell id='Name'>loop_query_2</cell><cell id='Datatype'>java.lang.String</cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>GET</cell><cell id='Default'></cell><cell id='Name'>loop_method</cell><cell id='Datatype'>java.lang.String</cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>COUPA_DEV01</cell><cell id='Default'></cell><cell id='Name'>loop_private_key</cell><cell id='Datatype'>java.lang.String</cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>https://ferring-dev.coupahost.com/api/invoices</cell><cell id='Default'></cell><cell id='Name'>loop_address</cell><cell id='Datatype'>java.lang.String</cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'></cell><cell id='Default'></cell><cell id='Name'>loop_sub_address</cell><cell id='Datatype'>java.lang.String</cell></row>")) {
					int i = 0;
					i++;
				}
				xmlValue = xmlValue.trim();
				if ("".equals(xmlValue)) {
					continue;
				}
				if (xmlValue.contains(":=:") && xmlValue.contains(":;")) {
					// this is the new syntax
					String[] sequenceValue = xmlValue.split(";");
					String name = null;
					String type = null;
					String datatype = null;
					for (String pair : sequenceValue) {
						String[] split = pair.split(":");
						if (split.length < 3) {
							continue;
						}
						if (pair.startsWith("Name")) {
							name = split[2];
						} else if (pair.startsWith("Type")) {
							type = split[2];
						} else if (pair.startsWith("Datatype")) {
							datatype = split[2];
						}
					}
					if ("expression".equalsIgnoreCase(type) && (datatype == null || "".equals(datatype.trim()))) {
						consumer.consume(new UndeclaredContentModifierDatatypeIssue(tag, String.format(
								"iflow [%s] has a content modifier of type [%s] with variable [%s] defined without datatype",
								tag.getId(), type, name)));
					}
					continue;
				}
				//we don't care about the value itself, sometimes the value might contain special characters that would need to be xml encoded such as &, so no need for that checks when all is needed is to check the type
				xmlValue = xmlValue.replaceAll("<cell id='Value'>(.*)</cell>","<cell id='Value'></cell>");
				// Parse the document.
				Processor p = new Processor(false);
				XdmNode docRoot;
				String rootedXmlValue = null;
				try {
					rootedXmlValue = "<root>" + xmlValue + "</root>";
					docRoot = p.newDocumentBuilder()
							.build(new StreamSource(new ByteArrayInputStream(rootedXmlValue.getBytes())));
				
					XPathCompiler xpathCompiler = p.newXPathCompiler();
					XdmValue typeAndDatatypePair = xpathCompiler
							.evaluate("//cell[@id='Name' or @id='Datatype' or @id='Type']", docRoot);
					if (typeAndDatatypePair != null && typeAndDatatypePair.size() % 3 == 0) {
						for (int i = 0; i < typeAndDatatypePair.size(); i = i + 3) {
							List<XdmItem> sublist = new ArrayList<>();
							sublist.add(typeAndDatatypePair.itemAt(i));
							sublist.add(typeAndDatatypePair.itemAt(i + 1));
							sublist.add(typeAndDatatypePair.itemAt(i + 2));
							XdmItem type = findElementWithIdAttributeValue("Type", sublist);
							XdmItem name = findElementWithIdAttributeValue("Name", sublist);
							XdmItem datatype = findElementWithIdAttributeValue("Datatype", sublist);

							if ("expression".equalsIgnoreCase(type.getUnderlyingValue().getStringValue())
									&& (datatype == null || datatype.getStringValue() == null
											|| "".equals(datatype.getStringValue().trim()))) {
								consumer.consume(new UndeclaredContentModifierDatatypeIssue(tag, String.format(
										"iflow [%s] has a content modifier of type [%s] with variable [%s] defined without datatype",
										tag.getId(), type.getUnderlyingValue().getStringValue(),
										name.getUnderlyingValue().getStringValue())));
							}
						}

					}
				} catch (SaxonApiException e) {
					throw new IflowXmlError("Error while processing content modifier XML", e);
				}
			}
		}
	}

	private XdmItem findElementWithIdAttributeValue(String attr, List<XdmItem> sublist) {
		for (XdmItem item : sublist) {
			if (item.toString().contains("id='" + attr + "'") || item.toString().contains("id=\"" + attr + "\"")) {
				return item;
			}
		}
		return null;
	}

}
