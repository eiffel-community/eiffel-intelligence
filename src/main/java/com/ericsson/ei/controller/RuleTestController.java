
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class is used for anything rule test related. It's currently used to test rules on Eiffel events. Test rules must be enabled in Eiffel Intelligence for the /rule-test endpoints to work.
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/rule-test", produces = "application/json")
public interface RuleTestController {


    /**
     * This method checks if the possibility to test rules has been enabled in Eiffel Intelligence.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getRuleTest(HttpServletRequest httpRequest);

    /**
     * This method extracts data from a single Eiffel event based on the given JMESPath expression.
     * 
     */
    @RequestMapping(value = "/run-single-rule", method = RequestMethod.POST)
    public ResponseEntity<?> createRuleTestRunSingleRule(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RuleCheckBody ruleCheckBody, HttpServletRequest httpRequest);

    /**
     * This method extracts data from the given list of Eiffel events, based on a set of rules and returns an aggregated object.
     * 
     */
    @RequestMapping(value = "/run-full-aggregation", method = RequestMethod.POST)
    public ResponseEntity<?> createRuleTestRunFullAggregation(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RulesCheckBody rulesCheckBody, HttpServletRequest httpRequest);

}
