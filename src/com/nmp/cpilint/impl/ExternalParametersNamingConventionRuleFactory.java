package com.nmp.cpilint.impl;

import java.util.regex.Pattern;

import org.dom4j.Element;

import dk.mwittrock.cpilint.RulesFileError;
import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactory;
import dk.mwittrock.cpilint.rules.RuleFactoryError;

public final class ExternalParametersNamingConventionRuleFactory implements RuleFactory {

	private static final String RULE_ELEMENT_NAME = "external-parameters-naming-convention";
	private static final String PATTERN_ELEMENT_NAME = "naming-pattern";

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals(RULE_ELEMENT_NAME);
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(
					String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		if (e.elements(PATTERN_ELEMENT_NAME).isEmpty()) {
			throw new RulesFileError(
					String.format("Element '%s' must contain exactly one '%s' element",
							RULE_ELEMENT_NAME, PATTERN_ELEMENT_NAME));
		}
		try {
			Pattern namingPattern = Pattern.compile(e.elements(PATTERN_ELEMENT_NAME).get(0).getText());
			return new ExternalParametersNamingConventionRule(namingPattern);
		} catch (Exception ex) {
			throw new RulesFileError(
					String.format("Element '%s' must contain a valid Regular Expression (RegEx)",
							PATTERN_ELEMENT_NAME));
		}
	}

}
