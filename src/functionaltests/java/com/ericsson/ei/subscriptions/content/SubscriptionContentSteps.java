package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequests;

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
    int applicationPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionContentSteps.class);
    private HttpRequests request;
    private ResponseEntity response;

    private String validSubscriptionFile = "src/functionaltests/resources/ValidSubscription.json";
    private String invalidSubscriptionFile = "src/functionaltests/resources/InvalidSubscription.json";

    ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void setUp() {
        request = new HttpRequests(applicationPort);
    }


    // SCENARIO 1


    @Given("^No subscriptions exist$")
    public void fetch_subscriptions() {
        String url = "http://localhost:" + applicationPort + "/subscriptions";
        response = request.makeHttpGetRequest(url);
        assertEquals("[]", response.getBody().toString());
    }

    @When("^Create subscription request$")
    public void send_subscription_request() {
        String url = "http://localhost:" + applicationPort + "/subscriptions";
        response = request.makeHttpPostRequest(url, validSubscriptionFile);
    }

    @Then("^The subscription is created successfully$")
    public void subscription_created_successfully() {
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @And("^Valid subscription exists$")
    public void ensure_valid_subscription_exists() {
        String url = "http://localhost:" + applicationPort + "/subscriptions/mySubscription";
        response = request.makeHttpGetRequest(url);
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }


    // SCENARIO 2


    @Given("^Subscription ([A-Za-z0-9_]+) already exists$")
    public void check_a_subscription_exists(String subscriptionName) {
        String url = "http://localhost:" + applicationPort + "/subscriptions/" + subscriptionName;
        response = request.makeHttpGetRequest(url);
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @When("^I create a duplicate subscription$")
    public void create_duplicate_subscription() {
        String url = "http://localhost:" + applicationPort + "/subscriptions";
        response = request.makeHttpPostRequest(url, validSubscriptionFile);
    }

    @Then("^The new subscription is rejected$")
    public void duplicate_subscription_is_rejected() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
    }

    @And("^([A-Za-z0-9_]+) is not duplicated$")
    public void check_duplicate_was_not_created(String name) {
        String url = "http://localhost:" + applicationPort + "/subscriptions/" + name;
        Subscription[] subscription = null;

        response = request.makeHttpGetRequest(url);

        try {
            subscription = mapper.readValue(response.getBody().toString(), Subscription[].class);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Ensure only one subscription exists
        assertEquals(1, subscription.length);
    }


    // SCENARIO 3

    @Given("^I delete ([A-Za-z0-9_]+)$")
    public void delete_subscription(String subscriptionName) {
        SubscriptionResponse subscriptionResponse = null;
        String url = "http://localhost:" + applicationPort + "/subscriptions/mySubscription";

        subscriptionResponse = request.makeHttpDeleteRequest(url);
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }

    @And("^Subscriptions does not exist$")
    public void get_all_subscriptions() {
        ResponseEntity<String> response = null;
        String url = "http://localhost:" + applicationPort + "/subscriptions";

        response = request.makeHttpGetRequest(url);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("[]", response.getBody().toString());
    }

    @When("^I create an invalid subscription$")
    public void create_invalid_subscription() {
        String url = "http://localhost:" + applicationPort + "/subscriptions";
        response = request.makeHttpPostRequest(url, invalidSubscriptionFile);
    }

    @Then("^The invalid subscription is rejected$")
    public void invalid_subscription_is_rejected() {
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), response.getStatusCodeValue());
    }

    @And("^The invalid subscription does not exist$")
    public void ensure_invalid_subscription_not_exists() {
        String url = "http://localhost:" + applicationPort + "/subscriptions";
        String invalidName = "#Subscription-&-with-&-mal-&-formatted-&-name";

        response = request.makeHttpGetRequest(url + invalidName);
        assertEquals("[]", response.getBody().toString());
    }

}
