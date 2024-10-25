package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.rules.*;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

public final class DisallowedXPathPropertyOnFilterRuleFactory implements RuleFactory {

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("disallowed-filter-xpath-property");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		List<String> disallowedExpressions = e.elements("disallow").stream().map(n -> n.getText()).collect(Collectors.toList());
		return new DisallowedXPathPropertyOnFilterRule(disallowedExpressions);
	}

}
