package com.ericsson.ei.restendpoints;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
        "spring.data.mongodb.database: RestEndpointsTestSteps",
        "missedNotificationDataBaseName: RestEndpointsTestSteps-missedNotifications",
        "rabbitmq.exchange.name: RestEndpointsTestSteps-exchange",
        "rabbitmq.consumerName: RestEndpointsTestStepsConsumer" })
@AutoConfigureMockMvc
public class RestEndpointsTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEndpointsTestSteps.class);

    @LocalServerPort
    private int applicationPort;
    private ResponseEntity response;

    @Value("${spring.data.mongodb.database}")
    private String eiDatabaseName;

    @Value("${aggregated.collection.name}")
    private String aggrCollectionName;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabaseName;

    HttpRequest request;

    @Given("^A GET request is prepared$")
    public void a_GET_request_is_prepared() {
        request = new HttpRequest(HttpMethod.GET);
        setRequestDefaults();
    }

    @Given("^A POST request is prepared$")
    public void a_POST_request_is_prepared() {
        request = new HttpRequest(HttpMethod.GET);
        setRequestDefaults();
    }

    @Given("^A PUT request is prepared$")
    public void a_PUT_request_is_prepared() {
        request = new HttpRequest(HttpMethod.GET);
        setRequestDefaults();
    }

    @Given("^A DELETE request is prepared$")
    public void a_DELETE_request_is_prepared() {
        request = new HttpRequest(HttpMethod.GET);
        setRequestDefaults();
    }

    @When("^Perform request on endpoint \"([^\"]*)\"$")
    public void perform_a_request_on_endpoint(String endpoint) throws Throwable {
        response = request.setEndpoint(endpoint)
                          .performRequest();
    }

    @Then("^Request should get response code (\\d+)$")
    public void request_should_get_response_code(int expectedstatusCode) throws Throwable {
        int actualStatusCode = response.getStatusCodeValue();
        assertEquals("EI rest API status code: ", expectedstatusCode, actualStatusCode);
        response = null;
    }

    private void setRequestDefaults() {
        request.setHost(getHostName())
               .setPort(applicationPort)
               .addHeader("content-type", "application/json")
               .addHeader("Accept", "application/json");
    }
}
