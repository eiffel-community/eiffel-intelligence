package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
public class SubscriptionContentSteps extends FunctionalTestBase {

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private HttpRequest getRequest;
    private HttpRequest postRequest;
    private HttpRequest deleteRequest;
    private ResponseEntity response;
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void setUp() {
        getRequest = new HttpRequest(HttpMethod.GET);

        getRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/subscriptions");

        deleteRequest = new HttpRequest(HttpMethod.DELETE);
        deleteRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/subscriptions");

        postRequest = new HttpRequest(HttpMethod.POST);
        postRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/subscriptions")
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json");

    }

    // SCENARIO 1

    @Given("^No subscriptions exist$")
    public void fetch_subscriptions() throws Throwable {
        response = getRequest.performRequest();
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create subscription request with \"(.*)\"$")
    public void create_subscription_request(String validSubscriptionFile) throws Throwable {
        postRequest.setBody(new File(validSubscriptionFile));
        response = postRequest.performRequest();
    }

    @Then("^The subscription is created successfully$")
    public void the_subscription_is_created_successfully() throws Throwable {
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @And("^Valid subscription \"([A-Za-z0-9_]+)\" exists$")
    public void valid_subscription_exists(String subscriptionName) throws Throwable {
        getRequest.setEndpoint("/subscriptions/" + subscriptionName);
        response = getRequest.performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    // SCENARIO 2

    @Given("^Subscription \"([A-Za-z0-9_]+)\" already exists$")
    public void subscription_already_exists(String subscriptionName) throws Throwable {
        getRequest.setEndpoint("/subscriptions/" + subscriptionName);
        response = getRequest.performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @When("^I create a duplicate subscription with \"(.*)\"$")
    public void create_duplicate_subscription_with(String validSubscriptionFile) throws Throwable {
        postRequest.setBody(new File(validSubscriptionFile));
        response = postRequest.performRequest();
    }

    @Then("^Duplicate subscription is rejected$")
    public void duplicate_subscription_is_rejected() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @And("^\"([A-Za-z0-9_]+)\" is not duplicated$")
    public void is_not_duplicated(String name) throws Throwable {
        getRequest.setEndpoint("/subscriptions/" + name);
        response = getRequest.performRequest();
        GetSubscriptionResponse getSubscriptionResponse = mapper.readValue(response.getBody().toString(),
                GetSubscriptionResponse.class);
        assertEquals(1, getSubscriptionResponse.getFoundSubscriptions().size());
    }

    // SCENARIO 3

    @Given("^I delete \"([A-Za-z0-9_]+)\"$")
    public void delete_subscription(String subscriptionName) throws Throwable {
        deleteRequest.setEndpoint("/subscriptions/" + subscriptionName);
        response = deleteRequest.performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @And("^Subscriptions does not exist$")
    public void subscriptions_does_not_exist() throws Throwable {
        getRequest.setEndpoint("/subscriptions");
        response = getRequest.performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create an invalid subscription with \"(.*)\"$")
    public void create_invalid_subscription_with(String invalidSubscriptionFile) throws Throwable {
        postRequest.setBody(new File(invalidSubscriptionFile));
        response = postRequest.performRequest();
    }

    @Then("^The invalid subscription is rejected$")
    public void invalid_subscription_is_rejected() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @And("^The invalid subscription does not exist$")
    public void invalid_subscription_does_not_exist() throws Throwable {
        String invalidName = "#Subscription-&-with-&-mal-&-formatted-&-name";
        getRequest.setEndpoint("/subscriptions/" + invalidName);
        response = getRequest.performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("[]", response.getBody().toString());
    }

}
