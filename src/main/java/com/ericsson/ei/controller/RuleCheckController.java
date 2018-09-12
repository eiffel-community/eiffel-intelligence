
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/rules/rule-check", produces = "application/json")
public interface RuleCheckController {


    /**
     * This call for run the jmespath rule object or rule on the JSON object
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> updateRulesRuleCheck(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RuleCheckBody ruleCheckBody);

    /**
     * This call for run the jmespath rule objects on the Json array of objects, we get aggregation Object as output
     * 
     */
    @RequestMapping(value = "/aggregation", method = RequestMethod.POST)
    public ResponseEntity<?> updateAggregation(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        RulesCheckBody rulesCheckBody);

    /**
     * This call for the current status of test rule entry point, we get status as output
     * 
     */
    @RequestMapping(value = "/testRulePageEnabled", method = RequestMethod.GET)
    public ResponseEntity<?> getTestRulePageEnabled();

}
