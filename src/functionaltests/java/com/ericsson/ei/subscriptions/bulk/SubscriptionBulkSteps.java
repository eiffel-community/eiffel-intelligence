package com.ericsson.ei.subscriptions.bulk;

import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
@TestPropertySource(properties = {"logging.level.com.ericsson.ei.subscriptions.bulk=DEBUG"})
public class SubscriptionBulkSteps extends FunctionalTestBase {

    private static final String TEST_RESOURCES_PATH = "src/functionaltests/resources";

    @LocalServerPort
    private int port;

    private String hostName = getHostName();
    private JSONArray subscriptions;
    private JSONArray retrievedSubscriptions;
    private ResponseEntity response;

    @Given("^file with subscriptions \"([^\"]*)\"$")
    public void file_with_subscriptions(String subscriptionsFileName) throws Throwable {
        String fileContent = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + subscriptionsFileName), "UTF-8");
        subscriptions = new JSONArray(fileContent);
    }

    @When("^make a POST request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_POST_request_with_list_of_subscriptions_to_the_subscription_REST_API(String endpoint) throws Throwable {
        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        response = postRequest.setHost(hostName)
                .setPort(port)
                .setEndpoint(endpoint)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(subscriptions.toString())
                .performRequest();
    }

    @When("^make a GET request with list of subscriptions names \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void make_a_GET_request_with_list_of_subscriptions_names_to_the_subscription_REST_API(
            String subscriptionsNamesList, String endpoint) throws Throwable {

        HttpRequest getRequest = new HttpRequest(HttpMethod.GET);
        response = getRequest.setHost(hostName)
                .setPort(port)
                .setEndpoint(endpoint + "/" + subscriptionsNamesList)
                .addHeader("Accept", "application/json")
                .performRequest();
    }

    @When("^make a DELETE request with list of subscriptions names \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void make_a_DELETE_request_with_list_of_subscriptions_names_to_the_subscription_REST_API(
            String subscriptionsNamesList, String endpoint) throws Throwable {

        HttpRequest deleteRequest = new HttpRequest(HttpMethod.DELETE);
        response = deleteRequest.setHost(hostName)
                .setPort(port)
                .setEndpoint(endpoint + "/" + subscriptionsNamesList)
                .addHeader("Accept", "application/json")
                .performRequest();
    }

    @When("^make a PUT request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_PUT_request_with_list_of_subscriptions_to_the_subscription_REST_API(
            String endpoint) throws Throwable {

        HttpRequest putRequest = new HttpRequest(HttpMethod.PUT);
        response = putRequest.setHost(hostName)
                .setPort(port)
                .setEndpoint(endpoint)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(subscriptions.toString())
                .performRequest();
    }

    @Then("^get response code of (\\d+)$")
    public void get_response_code_of(int statusCode) throws Throwable {
        assertEquals(statusCode, response.getStatusCode().value());
    }

    @Then("^get in response content (\\d+) found subscriptions and not found subscription name \"([^\"]*)\"$")
    public void get_in_response_content_found_subscriptions_and_not_found_subscription_name(
            int foundSubscriptionsNumber, String notFoundSubscriptionsName) throws Throwable {

        GetSubscriptionResponse subscriptionResponse = new ObjectMapper().readValue(
                response.getBody().toString(), GetSubscriptionResponse.class);

        assertEquals(foundSubscriptionsNumber,
                subscriptionResponse.getFoundSubscriptions().size());
        assertEquals(notFoundSubscriptionsName,
                subscriptionResponse.getNotFoundSubscriptions().get(0));
    }

    @Then("^get in response content subscription \"([^\"]*)\"$")
    public void get_in_response_content_subscription_and_reason(String subscriptionName) throws Throwable {
        JSONObject jsonResponse = new JSONArray(response.getBody().toString()).getJSONObject(0);
        assertEquals(subscriptionName, jsonResponse.getString("subscription"));
    }

    @Then("^number of retrieved subscriptions using REST API \"([^\"]*)\" is (\\d+)$")
    public void number_of_retrieved_subscriptions_using_REST_API_is(
            String endpoint, int subscriptionsNumber) throws Throwable {

        HttpRequest getRequest = new HttpRequest(HttpMethod.GET);
        response = getRequest.setHost(hostName)
                .setPort(port)
                .setEndpoint(endpoint)
                .addHeader("Accept", "application/json")
                .performRequest();
        retrievedSubscriptions = new JSONArray(response.getBody().toString());
        assertEquals(subscriptionsNumber, retrievedSubscriptions.length());
    }

    @Then("^retrieved subscriptions are same as given$")
    public void retrieved_subscriptions_are_same_as_given() throws Throwable {
        for (int i = 0; i < subscriptions.length(); i++) {
            assertEquals(subscriptions.getJSONObject(i).get("subscriptionName"),
                    retrievedSubscriptions.getJSONObject(i).get("subscriptionName"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationType"),
                    retrievedSubscriptions.getJSONObject(i).get("notificationType"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationMeta"),
                    retrievedSubscriptions.getJSONObject(i).get("notificationMeta"));
            assertEquals(true, retrievedSubscriptions.getJSONObject(i).has("userName"));
        }
    }
}
