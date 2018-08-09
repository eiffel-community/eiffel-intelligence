package com.ericsson.ei.subscriptions.authentication;

import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpDeleteRequest;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpGetRequest;
import com.ericsson.ei.utils.HttpPostRequest;
import com.ericsson.ei.utils.TestLDAPInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
@ContextConfiguration(initializers = TestLDAPInitializer.class)
public class AuthenticationSteps extends FunctionalTestBase {

    private static final String SUBSCRIPTION = "src/functionaltests/resources/subscription_single.json";

    @LocalServerPort
    private int applicationPort;
    private HttpRequest httpRequest;
    private ResponseEntity<String> response;

    @Given("^LDAP is activated$")
    public void ldap_is_activated() throws Throwable {
        String expectedContent = new JSONObject().put("security", true).toString();
        httpRequest = new HttpGetRequest();
        httpRequest.setUrl("http://localhost:").setPort(applicationPort).setEndpoint("/auth");
        response = httpRequest.build();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContent, response.getBody().toString());
    }

    @When("^a (\\w+) request is prepared for REST API \"(.*)\"")
    public void request_to_rest_api(String method, String endpoint) throws Throwable {
        switch (method) {
        case "POST":
            String requestBody = FileUtils.readFileToString(new File(SUBSCRIPTION), "UTF-8");
            httpRequest = new HttpPostRequest();
            httpRequest.setUrl("http://localhost:").setPort(applicationPort).setEndpoint(endpoint)
                    .setHeaders("Content-type", "application/json").setBody(requestBody);
            break;
        case "GET":
            httpRequest = new HttpGetRequest();
            httpRequest.setUrl("http://localhost:").setPort(applicationPort).setEndpoint(endpoint);
            break;
        }
    }
    
    @When("^username \"(\\w+)\" and password \"(\\w+)\" is used as credentials$")
    public void with_credentials(String username, String password) throws Throwable {
        String auth = username + ":" + password;
        String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()), "UTF-8");
        httpRequest.setHeaders("Authorization", "Basic " + encodedAuth);
    }
    
    @When("^request is sent$")
    public void request_sent() {
        response = httpRequest.build();
    }

    @Then("^response code (\\d+) is received")
    public void get_response_code(int statusCode) throws Throwable {
        assertEquals(HttpStatus.valueOf(statusCode), response.getStatusCode());
    }

    @Then("^subscription with name \"(\\w+)\" is(.*) created$")
    public void subscription_with_name_created(String subscriptionName, String check) throws Throwable {
        httpRequest = new HttpGetRequest();
        httpRequest.setUrl("http://localhost:").setPort(applicationPort)
                .setEndpoint("/subscriptions/" + subscriptionName);
        response = httpRequest.build();
        GetSubscriptionResponse subscription = new ObjectMapper().readValue(response.getBody().toString(),
                GetSubscriptionResponse.class);
        if (!check.isEmpty()) {
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(true, subscription.getFoundSubscriptions().isEmpty());
            assertEquals(subscriptionName, subscription.getNotFoundSubscriptions().get(0));
        } else {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(true, subscription.getNotFoundSubscriptions().isEmpty());
            assertEquals(subscriptionName, subscription.getFoundSubscriptions().get(0).getSubscriptionName());
        }
    }
}
