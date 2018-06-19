package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpDeleteRequest;
import com.ericsson.ei.utils.HttpPostRequest;
import com.ericsson.ei.utils.HttpGetRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


@Ignore
public class SubscriptionContentSteps extends FunctionalTestBase {

    @LocalServerPort
    private int applicationPort;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionContentSteps.class);
    private HttpGetRequest getRequest;
    private HttpPostRequest postRequest;
    private HttpDeleteRequest deleteRequest;
    private ResponseEntity response;
    private ObjectMapper mapper = new ObjectMapper();


    @PostConstruct
    private void setUp() {
        getRequest = new HttpGetRequest();
        getRequest.setPort(applicationPort).setUrl("http://localhost:").setEndpoint("/subscriptions");

        deleteRequest = new HttpDeleteRequest();
        deleteRequest.setPort(applicationPort).setUrl("http://localhost:").setEndpoint("/subscriptions");

        postRequest = new HttpPostRequest();
        postRequest.setPort(applicationPort)
                .setUrl("http://localhost:")
                .setEndpoint("/subscriptions")
                .setHeaders("content-type", "application/json")
                .setHeaders("Accept", "application/json");
    }

    // SCENARIO 1

    @Given("^No subscriptions exist$")
    public void fetch_subscriptions() {
        response = getRequest.build();
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create subscription request with \"(.*)\"$")
    public void create_subscription_request(String validSubscriptionFile) {
        postRequest.setParams(validSubscriptionFile);
        response = postRequest.build();
    }

    @Then("^The subscription is created successfully$")
    public void the_subscription_is_created_successfully() {
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @And("^Valid subscription \"([A-Za-z0-9_]+)\" exists$")
    public void valid_subscription_exists(String subscriptionName) {
        getRequest.setEndpoint("/subscriptions/" + subscriptionName);
        response = getRequest.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    // SCENARIO 2

    @Given("^Subscription \"([A-Za-z0-9_]+)\" already exists$")
    public void subscription_already_exists(String subscriptionName) {
        getRequest.setEndpoint("/subscriptions/" + subscriptionName);
        response = getRequest.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @When("^I create a duplicate subscription with \"(.*)\"$")
    public void create_duplicate_subscription_with(String validSubscriptionFile) {
        postRequest.setParams(validSubscriptionFile);
        response = postRequest.build();
    }

    @Then("^Duplicate subscription is rejected$")
    public void duplicate_subscription_is_rejected() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @And("^\"([A-Za-z0-9_]+)\" is not duplicated$")
    public void is_not_duplicated(String name) {
        Subscription[] subscription = null;
        getRequest.setEndpoint("/subscriptions/" + name);
        response = getRequest.build();

        try {
            subscription = mapper.readValue(response.getBody().toString(), Subscription[].class);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        // Ensure only one subscription exists
        assertEquals(1, subscription.length);
    }

    // SCENARIO 3

    @Given("^I delete \"([A-Za-z0-9_]+)\"$")
    public void delete_subscription(String subscriptionName) {
        SubscriptionResponse subscriptionResponse = null;
        deleteRequest.setEndpoint("/subscriptions/" + subscriptionName);
        subscriptionResponse = deleteRequest.build();
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }

    @And("^Subscriptions does not exist$")
    public void subscriptions_does_not_exist() {
        getRequest.setEndpoint("/subscriptions");
        response = getRequest.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create an invalid subscription with \"(.*)\"$")
    public void create_invalid_subscription_with(String invalidSubscriptionFile) {
        postRequest.setParams(invalidSubscriptionFile);
        response = postRequest.build();
    }

    @Then("^The invalid subscription is rejected$")
    public void invalid_subscription_is_rejected() {
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), response.getStatusCodeValue());
    }

    @And("^The invalid subscription does not exist$")
    public void invalid_subscription_does_not_exist() {
        String invalidName = "#Subscription-&-with-&-mal-&-formatted-&-name";
        getRequest.setEndpoint("/subscriptions/" + invalidName);
        response = getRequest.build();
        assertEquals("[]", response.getBody().toString());
    }

}
