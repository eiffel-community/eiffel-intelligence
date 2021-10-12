package com.ericsson.ei.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Ignore;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: EncryptionSteps",
        "failed.notifications.collection.name: EncryptionSteps-missedNotifications",
        "rabbitmq.exchange.name: EncryptionSteps-exchange",
        "rabbitmq.queue.suffix: EncryptionSteps"})
public class EncryptionSteps extends FunctionalTestBase {

    private static final Logger LOGGER = getLogger(EncryptionSteps.class);

    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";
    private static final String SUBSCRIPTION_GET_ENDPOINT = "/subscriptions/<name>";
    private static final String SUBSCRIPTION_ROOT_ENDPOINT = "/subscriptions";
    private static final String MOCK_ENDPOINT = "/notify-me";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTH_HEADER_VALUE = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
    private static final int NOTIFICATION_SLEEP = 5;

    private static final String SUBSCRIPTION_NAME = "MySubscription";
    private static final String MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final String NOTIFICATION_META = "http://localhost:{port}/notify-me";
    private static final String CONDITION_KEY = "jmespath";
    private static final String CONDITION_VALUE = "split(identity, '/') | [1] =='com.mycompany.myproduct'";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTH_TYPE = "BASIC_AUTH";

    @LocalServerPort
    private int applicationPort;

    private ResponseEntity response;
    private String mockServerPort;
    private ClientAndServer clientAndServer;

    @Before
    public void init() throws IOException {
        clientAndServer = ClientAndServer.startClientAndServer();
        mockServerPort = String.valueOf(clientAndServer.getLocalPort());
        setUpMock();
    }

    @When("^a subscription is created$")
    public void subscriptionIsCreated() throws Exception {
        LOGGER.debug("Creating a subscription.");
        RestPostSubscriptionObject subscription = new RestPostSubscriptionObject(SUBSCRIPTION_NAME);
        subscription.setNotificationMeta(NOTIFICATION_META.replace("{port}", mockServerPort));
        subscription.setRestPostBodyMediaType(MEDIA_TYPE);
        subscription.addNotificationMessageKeyValue("json", "dummy");
        JSONObject condition = new JSONObject().put(CONDITION_KEY, CONDITION_VALUE);
        subscription.addConditionToRequirement(0, condition);
        subscription.setUsername(USERNAME);
        subscription.setPassword(PASSWORD);
        subscription.setAuthenticationType(AUTH_TYPE);

        List<String> subscriptionsToSend = new ArrayList<>();
        subscriptionsToSend.add(subscription.toString());
        postSubscriptions(subscriptionsToSend.toString());
        validateSubscriptionsSuccessfullyAdded();
    }

    @When("^eiffel events are sent$")
    public void eiffelEventsAreSent() throws IOException, InterruptedException {
        LOGGER.debug("Sending Eiffel events.");
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
        List<String> eventsIdList = eventManager.getEventIdsList(EIFFEL_EVENTS_JSON_PATH,
                eventNamesToSend);
        List<String> missingEventIds = dbManager.verifyEventsInDB(eventsIdList, 0);
        String errorMessage = "The following events are missing in mongoDB: "
                + missingEventIds.toString();
        assertEquals(errorMessage, 0, missingEventIds.size());
    }

    @Then("^the password should be encrypted in the database$")
    public void passwordShouldBeEncrypted() {
        LOGGER.debug("Verifying encoded password in subscription.");
        String json = dbManager.getSubscription(SUBSCRIPTION_NAME);
        JSONObject subscription = new JSONObject(json);
        String password = subscription.getString("password");
        assertTrue(EncryptionUtils.isEncrypted(password));
    }

    @Then("^the subscription should trigger$")
    public void subscriptionShouldTrigger() throws InterruptedException {
        LOGGER.info("Verifying that subscription was triggered");
        TimeUnit.SECONDS.sleep(NOTIFICATION_SLEEP);
        clientAndServer.verify(request().withPath(MOCK_ENDPOINT).withBody(""),
                VerificationTimes.atLeast(1));
    }

    @Then("^the notification should be sent with a decrypted password$")
    public void notificationShouldBeSentWithDecryptedPassword() {
        LOGGER.info("Verifying that notification contains decrypted password");
        clientAndServer.verify(
                request().withPath(MOCK_ENDPOINT).withHeader(AUTH_HEADER_NAME, AUTH_HEADER_VALUE),
                VerificationTimes.atLeast(1));
    }

    private void postSubscriptions(String jsonDataAsString) throws Exception {
        HttpRequest postRequest = new HttpRequest(HttpRequest.HttpMethod.POST);
        response = postRequest.setHost(getHostName())
                              .setPort(applicationPort)
                              .addHeader("content-type", "application/json")
                              .addHeader("Accept", "application/json")
                              .setEndpoint(SUBSCRIPTION_ROOT_ENDPOINT)
                              .setBody(jsonDataAsString)
                              .performRequest();
        assertEquals("Expected to add subscription to EI", HttpStatus.OK.value(),
                response.getStatusCodeValue());
    }

    private void validateSubscriptionsSuccessfullyAdded()
            throws Exception {
        String endpoint = SUBSCRIPTION_GET_ENDPOINT.replaceAll("<name>", SUBSCRIPTION_NAME);
        HttpRequest getRequest = new HttpRequest(HttpRequest.HttpMethod.GET);
        response = getRequest.setHost(getHostName())
                             .setPort(applicationPort)
                             .addHeader("content-type", "application/json")
                             .addHeader("Accept", "application/json")
                             .setEndpoint(endpoint)
                             .performRequest();
        assertEquals("Subscription successfully added in EI: ", HttpStatus.OK.value(),
                response.getStatusCodeValue());
    }

    private List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        return eventNames;
    }

    private void setUpMock() {
        clientAndServer.when(request().withMethod("POST").withPath(MOCK_ENDPOINT))
                       .respond(response().withStatusCode(200).withBody(""));
    }
}
