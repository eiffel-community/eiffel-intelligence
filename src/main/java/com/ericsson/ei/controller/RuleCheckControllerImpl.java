package com.ericsson.ei.controller;

import java.io.IOException;

import com.ericsson.ei.controller.model.RuleCheckBody;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.services.IRuleCheckService;
import io.swagger.annotations.ApiParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class implements the Interface for JMESPath API, generated by RAML 0.8
 * Provides interaction with JmesPathInterface class
 * 
 * Usage: 1. If input contains a rule as an argument and json expression in a
 * text file, then the following curl command may be used. curl -H
 * "Content-type: application/x-www-form-urlencoded" -X POST --data-urlencode
 * jsonContent@testjson.txt
 * http://localhost:8090/jmespathrule/runRule?rule=data.outcome
 * 
 * 2. If input contains rule and json expression as two String arguments, then
 * the following curl command may be used. curl -H "Content-type:
 * application/x-www-form-urlencoded" -X POST -d
 * jsonContent={"data":{"outcome":{"conclusion":"SUCCESSFUL"},"test":"persistentLogs"}}
 * http://localhost:8090/jmespathrule/runRule?rule=data.outcome
 * 
 */

@Component
@CrossOrigin
@Api(value = "Check Rules", description = "REST endpoints for executing rule(s) on the JSON")
public class RuleCheckControllerImpl implements RuleCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionControllerImpl.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Value("${testaggregated.enabled:false}")
    private Boolean testEnable;

    /**
     * This method interacts with JmesPathInterface class method runRuleOnEvent
     * to evaluate a rule on JSON object.
     * 
     * @param rule-
     *            takes a String as a rule that need to be evaluated on JSON
     *            content
     * @param jsonContent-
     *            takes JSON object as a String
     * @return a String object
     * 
     */
    @Override
    @CrossOrigin
    @ApiOperation(value = "To execute rule on JSON")
    public ResponseEntity<?> updateRulesRuleCheck(@ApiParam(value = "JMESPath rule", required = true) @RequestParam String rule,
                                                  @ApiParam(value = "JSON object", required = true) @RequestBody String jsonContent) {
        String res = "[]";
        try {
            JSONObject jsonObj = new JSONObject(jsonContent);
            String jsonString = jsonObj.toString();
            res = jmesPathInterface.runRuleOnEvent(rule, jsonString).toString();
            LOGGER.debug("Query: " + rule + " executed successfully");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To execute the list of rules on list of Eiffel events. Return the aggregated object(s)")
    public ResponseEntity<?> updateAggregation(@ApiParam(value = "Object that include list of rules and list of Eiffel events", required = true)
                                                   @RequestBody RuleCheckBody body) {
        if (testEnable) {
            try {
                String aggregatedObject = ruleCheckService.prepareAggregatedObject(new JSONArray(body.getListRulesJson()), new JSONArray(body.getListEventsJson()));
                if (aggregatedObject != null) {
                    return new ResponseEntity<>(aggregatedObject, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Invalid JSON content", HttpStatus.BAD_REQUEST);
                }
            } catch (JSONException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return new ResponseEntity<>("Invalid JSON content", HttpStatus.BAD_REQUEST);
            }
        } else {
            LOGGER.debug("testaggregated.controller.enabled is not enabled in application.properties file, Unable to test the rules on list of events");
            return new ResponseEntity<String>("Please use the test environment for this execution", HttpStatus.BAD_REQUEST);
        }
    }

}
