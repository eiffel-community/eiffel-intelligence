package com.ericsson.ei.restendpoints;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.ericsson.eiffelcommons.constants.MediaType;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;



@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: RestEndpointsTestSteps",
        "failed.notifications.collection.name: RestEndpointsTestSteps-failedNotifications",
        "rabbitmq.exchange.name: RestEndpointsTestSteps-exchange",
        "rabbitmq.queue.suffix: RestEndpointsTestSteps" })
@AutoConfigureMockMvc
public class RestEndpointsTestSteps extends FunctionalTestBase {

    @LocalServerPort
    private int applicationPort;
    private ResponseEntity response;

    HttpRequest request;

    @Given("^A GET request is prepared$")
    public void a_GET_request_is_prepared() {
        request = new HttpRequest(HttpMethod.GET);
        setRequestDefaults();
    }

    @Given("^A POST request is prepared$")
    public void a_POST_request_is_prepared() {
        request = new HttpRequest(HttpMethod.POST);
        setRequestDefaults();
    }

    @Given("^A PUT request is prepared$")
    public void a_PUT_request_is_prepared() {
        request = new HttpRequest(HttpMethod.PUT);
        setRequestDefaults();
    }

    @Given("^A DELETE request is prepared$")
    public void a_DELETE_request_is_prepared() {
        request = new HttpRequest(HttpMethod.DELETE);
        setRequestDefaults();
    }

    @Given("^\"([^\"]*)\" add subscription with name \"([^\"]*)\" to the request body$")
    public void add_subscription_with_name_to_the_request_body(String doAdd, String subscriptionName) throws Throwable {
        if (doAdd.equals("do not")) {
            return;
        }
        RestPostSubscriptionObject restPostSubscription = new RestPostSubscriptionObject(subscriptionName);
        restPostSubscription.setNotificationMeta("some_url")
                            .setAuthenticationType("NO_AUTH")
                            .setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED);
        request.setBody(restPostSubscription.getAsSubscriptions().toString());
    }

    @Given("^Event rule json data is added as body$")
    public void event_rule_json_data_is_added_as_body() {
        String body = "{\"event\":{}, \"rule\":{}}";
        request.setBody(body);
    }

    @When("^Perform request on endpoint \"([^\"]*)\"$")
    public void perform_request_on_endpoint(String endpoint) throws Throwable {
        response = request.setEndpoint(endpoint)
                          .performRequest();
    }

    @Then("^Request should get response code (\\d+)$")
    public void request_should_get_response_code(int expectedStatusCode) throws Throwable {
        int actualStatusCode = response.getStatusCodeValue();
        assertEquals("EI rest API status code: ", expectedStatusCode, actualStatusCode);
        response = null;
    }

    private void setRequestDefaults() {
        request.setHost(getHostName())
               .setPort(applicationPort)
               .addHeader("content-type", "application/json")
               .addHeader("Accept", "application/json");
    }
}
