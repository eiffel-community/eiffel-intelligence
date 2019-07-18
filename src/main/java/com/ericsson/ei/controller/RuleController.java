
package com.ericsson.ei.controller;

import com.ericsson.ei.controller.model.RuleCheckBody;
import com.ericsson.ei.controller.model.RulesCheckBody;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class is used for anything rules related. It's currently used to test rules on eiffel events and to fetch the active rules file content. Test rules must be enabled in Eiffel Intelligence for the /rule-check endpoints to work.
 * (Generated with springmvc-raml-parser v.2.0.4)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/rules", produces = "application/json")
public interface RuleController {


    /**
     * This method returns the active rules file content.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getRules();

    /**
     * This method extracts data from a single Eiffel event based on the given JMESPath expression.
     * 
     */
    @RequestMapping(value = "/rule-check", method = RequestMethod.POST)
    public ResponseEntity<?> createRulesRuleCheck(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RuleCheckBody ruleCheckBody);

    /**
     * This method extracts data from the given list of Eiffel events, based on a set of rules and returns an aggregated object.
     * 
     */
    @RequestMapping(value = "/rule-check/aggregation", method = RequestMethod.POST)
    public ResponseEntity<?> createRuleCheckAggregation(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RulesCheckBody rulesCheckBody);

    /**
     * This method checks if the possibility to test rules has been enabled in Eiffel Intelligence.
     * 
     */
    @RequestMapping(value = "/rule-check/testRulePageEnabled", method = RequestMethod.GET)
    public ResponseEntity<?> getRuleCheckTestRulePageEnabled();

}
