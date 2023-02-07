package com.nmp.cpilint.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactory;
import dk.mwittrock.cpilint.rules.RuleFactoryError;

public final class CsrfProtectionRequiredWithExcludeRuleFactory implements RuleFactory {

	@Override 
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("csrf-protection-required-with-exclude");
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
		return new CsrfProtectionRequiredWithExcludeRule(exclusions, inclusions);
	}

}
