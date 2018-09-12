package com.ericsson.ei.subscriptions.authentication;

import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpExecutor;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.ericsson.ei.utils.TestLDAPInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.Before;
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
    private static final String SUBSCRIPTION_NAME = "Subscription_Test";
    private static final String X_AUTH_TOKEN = "X-Auth-Token";

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private HttpRequest httpRequest;
    private ResponseEntity<String> response;
    private String token;

    @Before("@RESTWithCredentials,@RESTWithSessionCookie")
    public void beforeScenario() throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/auth/logout");

        String auth = "gauss:password";
        String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()), "UTF-8");
        httpRequest.addHeader("Authorization", "Basic " + encodedAuth);
        httpRequest.performRequest();
    }

    @Before("@RESTWithTokenId")
    public void beforeScenarioSecond() {
        client_is_replaced();
    }


    @Given("^LDAP is activated$")
    public void ldap_is_activated() throws Throwable {
        String expectedContent = new JSONObject().put("security", true).toString();
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/auth");

        response = httpRequest.performRequest();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContent, response.getBody().toString());
    }

    @When("^a (\\w+) request is prepared for REST API \"(.*)\"$")
    public void request_to_rest_api(String method, String endpoint) throws Throwable {
        switch (method) {
        case "POST":
            String requestBody = FileUtils.readFileToString(new File(SUBSCRIPTION), "UTF-8");
            httpRequest = new HttpRequest(HttpMethod.POST);
            httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endpoint)
                    .addHeader("Content-type", "application/json").setBody(requestBody);
            break;
        case "GET":
            httpRequest = new HttpRequest(HttpMethod.GET);
            httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endpoint);
            break;
        }
    }

    @When("^username \"(\\w+)\" and password \"(\\w+)\" is used as credentials$")
    public void with_credentials(String username, String password) throws Throwable {
        String auth = username + ":" + password;
        String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()), "UTF-8");
        httpRequest.addHeader("Authorization", "Basic " + encodedAuth);
    }

    @When("^request is sent$")
    public void request_sent() throws Throwable {
        response = httpRequest.performRequest();
    }

    @When("^authentication token is attached$")
    public void auth_token_attached() throws Throwable {
        httpRequest.addHeader(X_AUTH_TOKEN, token);
    }

    @Then("^response code (\\d+) is received$")
    public void get_response_code(int statusCode) throws Throwable {
        assertEquals(HttpStatus.valueOf(statusCode), response.getStatusCode());
    }

    @Then("^subscription is(.*) created$")
    public void subscription_with_name_created(String check) throws Throwable {
        httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint("/subscriptions/" + SUBSCRIPTION_NAME);

        response = httpRequest.performRequest();
        GetSubscriptionResponse subscription = new ObjectMapper().readValue(response.getBody().toString(),
                GetSubscriptionResponse.class);
        if (!check.isEmpty()) {
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(true, subscription.getFoundSubscriptions().isEmpty());
            assertEquals(SUBSCRIPTION_NAME, subscription.getNotFoundSubscriptions().get(0));
        } else {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(true, subscription.getNotFoundSubscriptions().isEmpty());
            assertEquals(SUBSCRIPTION_NAME, subscription.getFoundSubscriptions().get(0).getSubscriptionName());
        }
    }

    @Then("^authentication token is saved$")
    public void auth_token_saved() {
        token = response.getHeaders().getFirst(X_AUTH_TOKEN);
    }

    @Then("^client is replaced$")
    public void client_is_replaced() {
        HttpExecutor.getInstance().recreateHttpClient();
    }

}
