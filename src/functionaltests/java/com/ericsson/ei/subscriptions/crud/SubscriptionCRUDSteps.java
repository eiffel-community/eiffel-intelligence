package com.ericsson.ei.subscriptions.crud;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionCRUDSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCRUDSteps.class);

    private static final String SUBSCRIPTION_FILE_PATH = "src/functionaltests/resources/subscription_single.json";
    private static final String SUBSCRIPTION_UPDATED_FILE_PATH = "src/functionaltests/resources/subscription_single_updated.json";

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private HttpRequest httpRequest;
    private ResponseEntity<String> response;

    private ObjectMapper mapper = new ObjectMapper();
    private static JSONArray jsonArray = null;

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint);
        response = httpRequest.performRequest();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @When("^I make a POST request with valid \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_POST_request_with_valid_to_the_subscription_REST_API(String arg1, String endPoint)
            throws Throwable {
        String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        httpRequest = new HttpRequest(HttpMethod.POST);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint)
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json")
                .setBody(jsonArray.toString());
        response = httpRequest.performRequest();
    }

    @Then("^I get response code of (\\d+)")
    public void i_get_response_code_of(int statusCode) {
        assertEquals(HttpStatus.valueOf(statusCode), response.getStatusCode());
    }
    /// Scenario:1 ends ==================================================================

    @When("^I make a GET request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_GET_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint)
            throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint + name);
        response = httpRequest.performRequest();
    }

    @Then("^Subscription name is \"([^\"]*)\"$")
    public void i_get_response_code_of_and_subscription_name(String name) throws Throwable {
        JsonNode node = eventManager.getJSONFromString(response.getBody());
        String found = node.get("foundSubscriptions").get(0).toString();
        Subscription subscription = mapper.readValue(found, Subscription.class);
        assertEquals(name, subscription.getSubscriptionName());
    }
    // Scenario:2 ends ==================================================================

    @When("^I make a PUT request with modified notificationType as \"([^\"]*)\" to REST API \"([^\"]*)\"$")
    public void i_make_a_PUT_request_with_modified_notificationType_as_to_REST_API(String notificationType,
            String endPoint) throws Throwable {
        String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_UPDATED_FILE_PATH), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        httpRequest = new HttpRequest(HttpMethod.PUT);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint)
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json")
                .setBody(jsonArray.toString());
        response = httpRequest.performRequest();
        LOGGER.info("Modified notificationType is:" + notificationType);
    }

    @Then("^I can validate modified notificationType \"([^\"]*)\" with GET request at \"([^\"]*)\"$")
    public void i_can_validate_modified_notificationType_with_GET_request_at(String notificationType, String endPoint)
            throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint);
        response = httpRequest.performRequest();
        JsonNode node = eventManager.getJSONFromString(response.getBody());
        String found = node.get("foundSubscriptions").get(0).toString();
        Subscription subscription = mapper.readValue(found, Subscription.class);
        assertEquals(notificationType, subscription.getNotificationType());
    }
    // Scenario:3 ends ==================================================================

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name,
            String endPoint) throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.DELETE);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint + name)
                .addHeader("Accept", "application/json");
        response = httpRequest.performRequest();
    }

    @Then("^My GET request with subscription name \"([^\"]*)\" at REST API \"([^\"]*)\" returns an empty String$")
    public void my_GET_request_with_subscription_name_at_REST_API_returns_empty_String(String name, String endPoint)
            throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint + name)
                .addHeader("Accept", "application/json");
        response = httpRequest.performRequest();
        JsonNode node = eventManager.getJSONFromString(response.getBody());
        int size = node.get("foundSubscriptions").size();
        String notFound = node.get("notFoundSubscriptions").get(0).asText();
        assertEquals("Subscription was found, should be empty",0, size);
        assertEquals(name, notFound);
    }
    // Scenario:4 ends ==================================================================
}