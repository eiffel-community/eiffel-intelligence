package com.ericsson.ei.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

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

@Component
@CrossOrigin
@Api(value = "Rule test", tags = {"Templates"})
public class RuleTestControllerImpl implements RuleTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleTestControllerImpl.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Setter
    @Value("${test.aggregation.enabled:false}")
    private Boolean testEnabled;

    @Value("${rules.path}")
    private String rulesPath;

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
    public ResponseEntity<?> createRuleTestRunSingleRule (
            @ApiParam(value = "JSON object", required = true) @RequestBody RuleCheckBody body, final HttpServletRequest httpRequest) {
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
    public ResponseEntity<?> createRuleTestRunFullAggregation(
            @ApiParam(value = "Object that include list of rules and list of Eiffel events", required = true) @RequestBody RulesCheckBody body,
            final HttpServletRequest httpRequest) {
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
            } catch (JSONException | IOException e) {
                String errorMessage = "Internal Server Error: Failed to generate aggregated object.";
                LOGGER.error(errorMessage, e);
                String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
                return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            String errorMessage = "Test rules functionality is disabled in backend server. "
                    + "Configure \"test.aggregation.enabled\" setting in backend servers properties "
                    + "to enable this functionality. This should normally only be enabled in backend test servers.";
            LOGGER.error(errorMessage);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @ApiOperation(value = "Check if rules check service is enabled", response = String.class)
    public ResponseEntity<?> getRuleTest(HttpServletRequest httpRequest) {
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
