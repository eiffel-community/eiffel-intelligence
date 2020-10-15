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
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.services.IRuleCheckService;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Setter;

@Component
@CrossOrigin
@Api(value = "Rules", tags = {"Rules"})
public class RuleControllerImpl implements RuleController{

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleControllerImpl.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Autowired
    private RulesHandler rulesHandler;

    @Setter
    @Value("${testaggregated.enabled:false}")
    private Boolean testEnabled;

    @Value("${rules.path}")
    private String rulesPath;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Get the active rules from Eiffel Intelligence", response = String.class)
    public ResponseEntity<?> getRules() {
        JsonNode rulesContent = rulesHandler.getRulesContent();
        ObjectMapper objectmapper = new ObjectMapper();
        try {
            String contentAsString = objectmapper.writeValueAsString(rulesContent);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("path", rulesPath);
            jsonResult.put("content", contentAsString);
            String result = jsonResult.toString();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            String errorMessage = "Internal Server Error: Failed to parse the rules content.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
    @ApiOperation(value = "To execute rule on one Eiffel event", response = String.class)
    public ResponseEntity<?> createRulesRuleCheck(
            @ApiParam(value = "JSON object", required = true) @RequestBody RuleCheckBody body) {
        JSONObject rule = new JSONObject(body.getRule().getAdditionalProperties());
        JSONObject event = new JSONObject(body.getEvent().getAdditionalProperties());

        String ruleString = rule.toString().replaceAll("\"", "");
        try {
            String res = jmesPathInterface.runRuleOnEvent(ruleString, event.toString()).toString();
            LOGGER.debug("Query: {} executed successfully", body.getRule());
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to run rule on event. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To execute the list of rules on list of Eiffel events. Returns the aggregated object(s)", response = String.class)
    public ResponseEntity<?> createRuleCheckAggregation(
            @ApiParam(value = "Object that include list of rules and list of Eiffel events", required = true) @RequestBody RulesCheckBody body) {
        if (testEnabled) {
            try {
                String aggregatedObject = ruleCheckService.prepareAggregatedObject(
                        new JSONArray(body.getListRulesJson()), new JSONArray(body.getListEventsJson()));
                if (aggregatedObject != null && !aggregatedObject.equals("[]")) {
                    return new ResponseEntity<>(aggregatedObject, HttpStatus.OK);
                } else {
                    String errorMessage = "Failed to generate aggregated object. List of rules or list of events are not correct";
                    LOGGER.error(errorMessage);
                    String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
                    return new ResponseEntity<>(errorJsonAsString, HttpStatus.BAD_REQUEST);
                }
            } catch (JSONException | IOException | SubscriptionValidationException e) {
                String errorMessage = "Internal Server Error: Failed to generate aggregated object.";
                LOGGER.error(errorMessage, e);
                String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
                return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            String errorMessage = "Test rules functionality is disabled in backend server. "
                    + "Configure \"testaggregated.enabled\" setting in backend servers properties "
                    + "to enable this functionality. This should normally only be enabled in backend test servers.";
            LOGGER.error(errorMessage);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Check if rules check service is enabled", response = String.class)
    public ResponseEntity<?> getRuleCheckTestRulePageEnabled() {
        LOGGER.debug("Getting Enabling Status of Rules Check Service");
        try {
            return new ResponseEntity<>(new JSONObject().put("status", testEnabled).toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to get status.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
