package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.rules.*;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

public final class UnusedParametersRuleFactory implements RuleFactory {

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("unused-parameters-rule");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		List<String> exclusions = e.elements("exclude").stream().map(n -> n.getText()).collect(Collectors.toList());
		return new UnusedParametersRule(exclusions);
	}

}
