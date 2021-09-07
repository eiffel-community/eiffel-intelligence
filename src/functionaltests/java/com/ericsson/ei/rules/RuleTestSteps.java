package com.ericsson.ei.rules;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.File;

import com.ericsson.ei.controller.RuleTestControllerImpl;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: RuleTestSteps",
        "failed.notifications.collection.name: RuleTestSteps-failedNotifications",
        "rabbitmq.exchange.name: RuleTestSteps-exchange",
        "rabbitmq.queue.suffix: RuleTestSteps" })
public class RuleTestSteps extends FunctionalTestBase {

    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    @Autowired
    private RuleTestControllerImpl ruleTestControllerImpl;

    private String rules;
    private String events;

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private ResponseEntity response;

    @Given("^rules checking is enabled$")
    public void rules_checking_is_enabled() {
        ruleTestControllerImpl.setTestEnabled(true);
    }

    @Given("^rules checking is not enabled$")
    public void rules_checking_is_not_enabled() {
        ruleTestControllerImpl.setTestEnabled(false);
    }

    @Given("^file with JMESPath rules \"([^\"]*)\" and file with events \"([^\"]*)\"$")
    public void file_with_JMESPath_rules_and_file_with_events(String rulesFileName, String eventsFileName)
            throws Throwable {
        rules = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + rulesFileName), "UTF-8");
        events = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + eventsFileName), "UTF-8");
    }

    @When("^make a POST request to the REST API \"([^\"]*)\" with a single rule")
    public void make_a_POST_request_to_the_REST_API_with_request_parameter(String endpoint) throws Throwable {
        String requestBody = new JSONObject().put("rule", new JSONObject(rules))
                                             .put("event", new JSONObject(events))
                                             .toString();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        response = postRequest.setPort(applicationPort)
                              .setHost(hostName)
                              .addHeader("content-type", "application/json")
                              .addHeader("Accept", "application/json")
                              .setEndpoint(endpoint)
                              .setBody(requestBody)
                              .performRequest();
    }

    @When("^make a POST request to the REST API \"([^\"]*)\"$")
    public void make_a_POST_request_to_the_REST_API(String endpoint) throws Throwable {
        String requestBody = new JSONObject().put("listRulesJson", new JSONArray(rules))
                                             .put("listEventsJson", new JSONArray(events))
                                             .toString();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        response = postRequest.setPort(applicationPort)
                              .setHost(hostName)
                              .addHeader("content-type", "application/json")
                              .addHeader("Accept", "application/json")
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
            for(int i=0;i<responseArray.length();i++) {
            	JSONObject jsonobject = responseArray.getJSONObject(i);
            	jsonobject.remove("Time");
            	}
            assertEquals(expectedArray, responseArray, true);
        } else {
            JSONObject expectedObject = new JSONObject(responseBody);
            JSONObject responseObject = new JSONObject(response.getBody().toString());
            assertEquals(expectedObject, responseObject, true);
        }
    }

    @Then("^get request from REST API \"([^\"]*)\" return response code of (\\d+) and status as \"([^\"]*)\"$")
    public void get_request_from_REST_API_return_response_code_of_and_status_as(String endpoint, int statusCode,
            String status) throws Throwable {
        String responseBody = new JSONObject().put("status", Boolean.valueOf(status)).toString();
        HttpRequest getRequest = new HttpRequest(HttpMethod.GET);
        ResponseEntity<String> apiResponse = getRequest.setPort(applicationPort)
                                                       .setHost(hostName)
                                                       .addHeader("content-type", "application/json")
                                                       .addHeader("Accept", "application/json")
                                                       .setEndpoint(endpoint)
                                                       .performRequest();

        assertEquals(statusCode, apiResponse.getStatusCodeValue());
        assertEquals(responseBody, apiResponse.getBody());
    }

}