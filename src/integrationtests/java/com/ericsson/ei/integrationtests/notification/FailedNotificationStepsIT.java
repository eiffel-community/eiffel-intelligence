package com.ericsson.ei.integrationtests.notification;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.eiffelcommons.subscriptionobject.MailSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.SubscriptionObject;
import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;
import com.ericsson.eiffelcommons.utils.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import util.IntegrationTestBase;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class })
@TestPropertySource(properties = { "spring.mail.port: 9999" })
public class FailedNotificationStepsIT extends IntegrationTestBase {
    private String rulesFilePath;
    private String eventsFilePath;
    private ObjectMapper objectMapper = new ObjectMapper();
    private int extraEventsCount = 0;

    private long startTime;

    private SubscriptionObject subscriptionObject;

    @Given("^the rules \"([^\"]*)\"$")
    public void the_rules(String rulesFilePath) throws Throwable {
        this.rulesFilePath = rulesFilePath;
    }

    @Given("^the events \"([^\"]*)\"$")
    public void the_events(String eventsFilePath) throws Throwable {
        this.eventsFilePath = eventsFilePath;
    }

    @Given("^subscription object of type \"([^\"]*)\" with name \"([^\"]*)\" is created$")
    public void subscription_object_for_with_name_is_created(String subscriptionType,
            String subscriptionName) throws Throwable {
        if (subscriptionType.equalsIgnoreCase("Mail")) {
            subscriptionObject = new MailSubscriptionObject(subscriptionName);
        } else {
            subscriptionObject = new RestPostSubscriptionObject(subscriptionName);
        }
    }

    @When("^notification meta \"([^\"]*)\" is set in subscription$")
    public void notification_meta_is_set_in_subscription(String notificationMeta) throws Throwable {
        subscriptionObject.setNotificationMeta(notificationMeta);
    }

    @When("^parameter form key \"([^\"]*)\" and form value \"([^\"]*)\" is added in subscription$")
    public void parameter_key_and_value_is_added_in_subscription(String formKey, String formValue) {
        subscriptionObject.addNotificationMessageKeyValue(formKey, formValue);
    }

    @When("^condition \"([^\"]*)\" at requirement index '(\\d+)' is added in subscription$")
    public void requirement_for_condition_is_added_in_subscription(String condition,
            int requirementIndex) throws Throwable {
        subscriptionObject.addConditionToRequirement(requirementIndex, new JSONObject().put(
                "jmespath", condition));
    }

    @When("^the eiffel events are sent$")
    public void eiffel_events_are_sent() throws Throwable {
        super.sendEventsAndConfirm();
    }

    @When("^rest post body media type is set to \"([^\"]*)\" is set in subscription$")
    public void rest_post_body_media_type_is_set_to_is_set_in_subscription(
            String restPostBodyMediaType) throws Throwable {
        subscriptionObject.setRestPostBodyMediaType(restPostBodyMediaType);
    }

    @Then("^subscription is uploaded$")
    public void subscription_is_uploaded()
            throws URISyntaxException, ClientProtocolException, IOException {
        assert (subscriptionObject instanceof RestPostSubscriptionObject
                || subscriptionObject instanceof MailSubscriptionObject) : "SubscriptionObject must have been initiated.";

        startTime = System.currentTimeMillis();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        postRequest.setBaseUrl("http://" + eiHost + ":" + port)
                   .setEndpoint("/subscriptions")
                   .addHeader("Content-type", "application/json")
                   .setBody(subscriptionObject.getAsSubscriptions().toString());

        ResponseEntity response = postRequest.performRequest();
        int statusCode = Integer.valueOf(response.getStatusCodeValue());
        assertEquals(200, statusCode);
    }

    @Then("^failed notification of type \"([^\"]*)\" should exist for subscription \"([^\"]*)\"$")
    public void failed_notification_exists(String searchValue, String subscriptionName) throws Throwable {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl("http://" + eiHost + ":" + port)
               .setEndpoint("/queryMissedNotifications")
               .addParam("subscriptionName", subscriptionName);
        
        ResponseEntity response = request.performRequest();
        String message = objectMapper.readTree(response.getBody()).get("queryResponseEntity").get("message").toString();
        assertEquals(true, message.contains(searchValue));
    }

    @Override
    protected String getRulesFilePath() {
        return rulesFilePath;
    }

    @Override
    protected String getEventsFilePath() {
        return eventsFilePath;
    }

    @Override
    protected Map<String, JsonNode> getCheckData() throws IOException {
        Map<String, JsonNode> checkData = new HashMap<>();
        return checkData;
    }

    @Override
    protected int extraEventsCount() {
        return extraEventsCount;
    }

    @Override
    protected List<String> getEventNamesToSend() throws IOException {
        ArrayList<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        return eventNames;
    }
}
