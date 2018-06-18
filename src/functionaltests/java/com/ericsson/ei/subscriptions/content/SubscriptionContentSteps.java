package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpDeleteRequests;
import com.ericsson.ei.utils.HttpPostRequests;
import com.ericsson.ei.utils.HttpGetRequests;

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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@Ignore
public class SubscriptionContentSteps extends FunctionalTestBase {

    @LocalServerPort
    private int applicationPort;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionContentSteps.class);
    private HttpGetRequests get;
    private HttpPostRequests post;
    private HttpDeleteRequests delete;
    private ResponseEntity response;
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> headerMap = new HashMap();


    @PostConstruct
    private void setUp() {
        get = new HttpGetRequests(applicationPort, "http://localhost:", "/subscriptions");
        delete = new HttpDeleteRequests(applicationPort, "http://localhost:", "/subscriptions");

        post = new HttpPostRequests(
                applicationPort,
                "http://localhost:",
                "/subscriptions");
        headerMap.put("content-type", "application/json");
        headerMap.put("Accept", "application/json");
        post.setHeaders(headerMap);
    }

    // SCENARIO 1

    @Given("^No subscriptions exist$")
    public void fetch_subscriptions() {
        response = get.build();
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create subscription request with \"(.*)\"$")
    public void create_subscription_request(String validSubscriptionFile) {
        post.setParams(validSubscriptionFile);
        response = post.build();
    }

    @Then("^The subscription is created successfully$")
    public void the_subscription_is_created_successfully() {
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @And("^Valid subscription \"([A-Za-z0-9_]+)\" exists$")
    public void valid_subscription_exists(String subscriptionName) {
        get.setEndpoint("/subscriptions/" + subscriptionName);
        response = get.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    // SCENARIO 2

    @Given("^Subscription \"([A-Za-z0-9_]+)\" already exists$")
    public void subscription_already_exists(String subscriptionName) {
        get.setEndpoint("/subscriptions/" + subscriptionName);
        response = get.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @When("^I create a duplicate subscription with \"(.*)\"$")
    public void create_duplicate_subscription_with(String validSubscriptionFile) {
        post.setParams(validSubscriptionFile);
        response = post.build();
    }

    @Then("^Duplicate subscription is rejected$")
    public void duplicate_subscription_is_rejected() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @And("^\"([A-Za-z0-9_]+)\" is not duplicated$")
    public void is_not_duplicated(String name) {
        Subscription[] subscription = null;
        get.setEndpoint("/subscriptions/" + name);
        response = get.build();

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
        delete.setEndpoint("/subscriptions/" + subscriptionName);
        subscriptionResponse = delete.build();
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }

    @And("^Subscriptions does not exist$")
    public void subscriptions_does_not_exist() {
        get.setEndpoint("/subscriptions");
        response = get.build();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create an invalid subscription with \"(.*)\"$")
    public void create_invalid_subscription_with(String invalidSubscriptionFile) {
        post.setParams(invalidSubscriptionFile);
        response = post.build();
    }

    @Then("^The invalid subscription is rejected$")
    public void invalid_subscription_is_rejected() {
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), response.getStatusCodeValue());
    }

    @And("^The invalid subscription does not exist$")
    public void invalid_subscription_does_not_exist() {
        String invalidName = "#Subscription-&-with-&-mal-&-formatted-&-name";
        get.setEndpoint("/subscriptions/" + invalidName);
        response = get.build();
        assertEquals("[]", response.getBody().toString());
    }

}
