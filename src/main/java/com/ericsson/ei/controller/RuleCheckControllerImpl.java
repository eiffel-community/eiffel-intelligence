/*
   Copyright 2018 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.controller;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.springframework.web.bind.annotation.RequestBody;

import com.ericsson.ei.controller.model.RuleCheckBody;
import com.ericsson.ei.controller.model.RulesCheckBody;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.services.IRuleCheckService;
import com.ericsson.ei.utils.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Setter;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCheckControllerImpl.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Setter
    @Value("${testaggregated.enabled:false}")
    private Boolean testEnable;

    /**
     * This method interacts with JmesPathInterface class method runRuleOnEvent to
     * evaluate a rule on JSON object.
     *
     * @param rule-
     *            takes a String as a rule that need to be evaluated on JSON content
     * @param jsonContent-
     *            takes JSON object as a String
     * @return a String object
     *
     */
    @Override
    @CrossOrigin
    @ApiOperation(value = "To execute rule on JSON", response = String.class)
    public ResponseEntity<?> createRulesRuleCheck(
            @ApiParam(value = "JSON object", required = true) @RequestBody RuleCheckBody body) {
        JSONObject rule = new JSONObject(body.getRule().getAdditionalProperties());
        JSONObject event = new JSONObject(body.getEvent().getAdditionalProperties());

        String ruleString = rule.toString().replaceAll("\"", "");
        try {

            String res = jmesPathInterface.runRuleOnEvent(ruleString, event.toString()).toString();
            LOGGER.debug("Query: " + body.getRule()+ " executed successfully");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to run rule on event. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, ExceptionUtils.getStackTrace(e));
            return new ResponseEntity<>(ResponseMessage.createJsonMessage(errorMessage), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To execute the list of rules on list of Eiffel events. Return the aggregated object(s)", response = String.class)
    public ResponseEntity<?> createRuleCheckAggregation(
            @ApiParam(value = "Object that include list of rules and list of Eiffel events", required = true) @RequestBody RulesCheckBody body) {
        if (testEnable) {
            try {
                String aggregatedObject = ruleCheckService.prepareAggregatedObject(
                        new JSONArray(body.getListRulesJson()), new JSONArray(body.getListEventsJson()));
                if (aggregatedObject != null && !aggregatedObject.equals("[]")) {
                    return new ResponseEntity<>(aggregatedObject, HttpStatus.OK);
                } else {
                    String errorMessage = "Failed to generate aggregated object. List of rules or list of events are not correct";
                    LOGGER.error(errorMessage);
                    return new ResponseEntity<>(ResponseMessage.createJsonMessage(errorMessage), HttpStatus.BAD_REQUEST);
                }
            } catch (JSONException | IOException e) {
                String errorMessage = "Internal Server Error: Failed to generate aggregated object.";
                LOGGER.error(errorMessage, ExceptionUtils.getStackTrace(e));
                return new ResponseEntity<>(ResponseMessage.createJsonMessage(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            String errorMessage = "Test Rules functionality is disabled in backend server. "
                    + "Configure \"testaggregated.controller.enabled\" setting in backend servers properties "
                    + "to enable this functionality. This should normally only be enabled in backend test servers.";
            LOGGER.error(errorMessage);
            return new ResponseEntity<>(ResponseMessage.createJsonMessage(errorMessage), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To get Rules Check Srvice enabled status", response = String.class)
    public ResponseEntity<?> getRuleCheckTestRulePageEnabled() {
        LOGGER.debug("Getting Enabling Status of Rules Check Service");
        try {
            return new ResponseEntity<>(new JSONObject().put("status", testEnable).toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to get Status.";
            LOGGER.error(errorMessage, ExceptionUtils.getStackTrace(e));
            return new ResponseEntity<>(ResponseMessage.createJsonMessage(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
