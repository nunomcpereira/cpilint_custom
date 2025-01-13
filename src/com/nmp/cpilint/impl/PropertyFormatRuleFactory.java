package com.nmp.cpilint.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import dk.mwittrock.cpilint.rules.*;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;

public final class PropertyFormatRuleFactory implements RuleFactory {

    @Override
    public boolean canCreateFrom(Element e) {
        return e.getName().equals("property-format");
    }

    @Override
    public Rule createFrom(Element e) {
        if (!canCreateFrom(e)) {
            throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
        }
        
        // Parse the exceptions
        String exceptions = e.elementText("exceptions");
        // Parse the <rule> elements
        List<Map<String, String>> rules = e.elements("rule").stream().map(ruleElement -> {
            String activationDate = ruleElement.elementText("activationDate");
            String ruleExpression = ruleElement.elementText("ruleExpression");

            if (activationDate == null || ruleExpression == null) {
                throw new RuleFactoryError("Each <rule> must contain both <activationDate> and <ruleExpression> elements.");
            }

            // Create a map to hold rule details
            Map<String, String> rule = Map.of(
                "activationDate", activationDate,
                "ruleExpression", ruleExpression,
                "exceptions", exceptions != null ? exceptions : "" // Default to empty string if null
            );
            return rule;
        }).collect(Collectors.toList());

        // Return the rule (you might adapt this to match your Rule logic)
        return new PropertyFormatRule(rules);
    }
}
