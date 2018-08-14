package com.ericsson.ei.rules;

import com.ericsson.ei.controller.RuleCheckController;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@Ignore
public class RuleCheckSteps extends FunctionalTestBase {

    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    @Autowired
    private RuleCheckController ruleCheckController;

    private String rules;
    private String events;

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private ResponseEntity response;

    @Given("^rules checking is enabled$")
    public void rules_checking_is_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", true);
    }

    @Given("^rules checking is not enabled$")
    public void rules_checking_is_not_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", false);
    }

    @Given("^file with JMESPath rules \"([^\"]*)\" and file with events \"([^\"]*)\"$")
    public void file_with_JMESPath_rules_and_file_with_events(String rulesFileName, String eventsFileName) throws Throwable {
        rules = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + rulesFileName), "UTF-8");
        events = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + eventsFileName), "UTF-8");
    }

    @When("^make a POST request to the REST API \"([^\"]*)\" with request parameter \"([^\"]*)\"$")
    public void make_a_POST_request_to_the_REST_API_with_request_parameter(String endpoint, String requestParam) throws Throwable {
        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        response = postRequest.setPort(applicationPort)
                .setHost(hostName)
                .setHeaders("content-type", "application/json")
                .setHeaders("Accept", "application/json")
                .setEndpoint(endpoint)
                .setBody(events)
                .setParam(requestParam, rules)
                .performRequest();
    }

    @When("^make a POST request to the REST API \"([^\"]*)\"$")
    public void make_a_POST_request_to_the_REST_API(String endpoint) throws Throwable {
        String requestBody = new JSONObject()
            .put("listRulesJson", new JSONArray(rules))
            .put("listEventsJson", new JSONArray(events))
            .toString();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        response = postRequest.setPort(applicationPort)
                .setHost(hostName)
                .setHeaders("content-type", "application/json")
                .setHeaders("Accept", "application/json")
                .setEndpoint(endpoint)
                .setBody(requestBody)
                .performRequest();
    }

    @Then("^get response code of (\\d+)$")
    public void get_response_code_of(int statusCode) throws Throwable {
        assertEquals(statusCode, response.getStatusCodeValue());
    }

    @Then("^get content \"([^\"]*)\"$")
    public void get_content(String contentFileName) throws Throwable {
        String responseBody = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + contentFileName), "UTF-8");

        Object expectedResponse = new JSONTokener(responseBody).nextValue();
        if (expectedResponse instanceof JSONArray) {
            JSONArray expectedArray = new JSONArray(responseBody);
            JSONArray responseArray = new JSONArray(response.getBody().toString());
            assertEquals(expectedArray, responseArray, true);
        } else {
            JSONObject expectedObject = new JSONObject(responseBody);
            JSONObject responseObject = new JSONObject(response.getBody().toString());
            assertEquals(expectedObject, responseObject, true);
        }
    }

}