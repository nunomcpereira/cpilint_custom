package com.nmp.cpilint.impl;

import org.dom4j.Element;

import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactory;
import dk.mwittrock.cpilint.rules.RuleFactoryError;

public final class DesignGuidelinesRuleFactory implements RuleFactory {

	private static final String ELEMENT_NAME = "design-guidelines-check";

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals(ELEMENT_NAME);
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		return new DesignGuidelinesRule();
	}

}
