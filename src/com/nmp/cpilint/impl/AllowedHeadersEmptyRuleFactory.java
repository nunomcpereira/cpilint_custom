package com.nmp.cpilint.impl;

import dk.mwittrock.cpilint.rules.*;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

public final class AllowedHeadersEmptyRuleFactory implements RuleFactory {

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("allowed-headers-empty");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		List<String> exclusions = e.elements("exclude").stream().map(n -> n.getText()).collect(Collectors.toList());
		List<String> inclusions = e.elements("include").stream().map(n -> n.getText()).collect(Collectors.toList());
		if (inclusions.size() > 0 && exclusions.size() > 0) {
			throw new RuleFactoryError(String.format(
					"Cannot create Rule with both exclusions and inclusions. Select only one mode. object from element '%s'",
					e.getName()));
		}
		return new AllowedHeadersEmptyRule(exclusions, inclusions);
	}

}
