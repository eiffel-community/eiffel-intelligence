package com.ericsson.ei.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import com.ericsson.ei.exception.InvalidRulesException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.IRuleTestService;
import com.ericsson.ei.utils.ResponseMessage;

import io.netty.util.internal.StringUtil;
import lombok.Setter;

@Tag(name = "Rule-test", description = "Test rules on Eiffel events")
@Component
@CrossOrigin
public class RuleTestControllerImpl implements RuleTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleTestControllerImpl.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private IRuleTestService ruleTestService;

    @Setter
    @Value("${test.aggregation.enabled:false}")
    private Boolean testEnabled;

    @Value("${rules.path}")
    private String rulesPath;

    /**
     * This method interacts with JmesPathInterface class method runRuleOnEvent to
     * evaluate a rule on JSON object.
     *
     * @param body - the request body contains a rule and an event
     * @return a String object
     *
     */
    @Override
    @CrossOrigin
    @Operation(summary = "Execute rule on one Eiffel event")
    public ResponseEntity<?> createRuleTestRunSingleRule (
            @RequestBody RuleCheckBody body, final HttpServletRequest httpRequest) {
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
    @Operation(summary = "Execute a list of rules on a list of Eiffel events")
    public ResponseEntity<?> createRuleTestRunFullAggregation(
            @RequestBody RulesCheckBody body,
            final HttpServletRequest httpRequest) {
        if (testEnabled) {
            String aggregatedObject = StringUtil.EMPTY_STRING;
            try {
                aggregatedObject = ruleTestService.prepareAggregatedObject(
                                new JSONArray(body.getListRulesJson()), new JSONArray(body.getListEventsJson()));
            } 
            catch (InvalidRulesException e) {
                String errorJsonAsString = ResponseMessage.createJsonMessage(e.getMessage());
                return new ResponseEntity<>(errorJsonAsString, HttpStatus.BAD_REQUEST);
            }
            catch (JSONException e) {
                String errorMessage = "Failed to generate aggregated object.";
                LOGGER.error(errorMessage, e);
                String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
                return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            catch (Exception e) {
                	String errorMessage = "Failed to generate aggregated object.";
                	LOGGER.error(errorMessage, e);
            }
            if (aggregatedObject != null && !aggregatedObject.equals("[]")) {
                    return new ResponseEntity<>(aggregatedObject, HttpStatus.OK);
             } else {
                    String errorMessage = "Failed to generate aggregated object. List of rules or list of events are not correct";
                    LOGGER.error(errorMessage);
                    String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
                    return new ResponseEntity<>(errorJsonAsString, HttpStatus.BAD_REQUEST);
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
    @Operation(summary = "Check if rules test service is enabled")
    public ResponseEntity<?> getRuleTest(HttpServletRequest httpRequest) {
        LOGGER.debug("Getting Enabled Status of Rules Test Service");
        try {
            return new ResponseEntity<>(new JSONObject().put("status", testEnabled).toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to get status.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
