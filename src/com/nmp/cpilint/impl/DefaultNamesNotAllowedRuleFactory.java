package com.nmp.cpilint.impl;
import dk.mwittrock.cpilint.rules.*;

import org.dom4j.Element;

public final class DefaultNamesNotAllowedRuleFactory implements RuleFactory {

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("default-names-not-allowed-rule");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		return new DefaultNamesNotAllowedRule();
	}

}
