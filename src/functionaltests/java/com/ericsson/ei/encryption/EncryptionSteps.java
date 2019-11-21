package com.ericsson.ei.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Ignore;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.EncryptionFormatter;
import com.ericsson.ei.utils.Encryptor;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = { "spring.data.mongodb.database: EncryptionSteps",
        "missedNotificationDataBaseName: EncryptionSteps-missedNotifications",
        "rabbitmq.exchange.name: EncryptionSteps-exchange",
        "rabbitmq.consumerName: EncryptionSteps-consumer",
        "jasypt.encryptor.password: secret" })
public class EncryptionSteps extends FunctionalTestBase {

    private static final Logger LOGGER = getLogger(EncryptionSteps.class);

    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";
    private static final String SUBSCRIPTION_PATH = "src/functionaltests/resources/subscription_auth.json";
    private static final String SUBSCRIPTION_GET_ENDPOINT = "/subscriptions/<name>";
    private static final String SUBSCRIPTION_ROOT_ENDPOINT = "/subscriptions";
    private static final String MOCK_ENDPOINT = "/notify-me";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTH_HEADER_VALUE = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

    @Autowired
    private Encryptor encryptor;

    @LocalServerPort
    private int applicationPort;

    private String subscriptionName;
    private ResponseEntity response;
    private String mockServerPort;
    private ClientAndServer clientAndServer;

    @Before
    public void init() throws IOException {
        clientAndServer = startClientAndServer();
        mockServerPort = String.valueOf(clientAndServer.getLocalPort());
        setUpMock();
    }

    @When("^a subscription is created$")
    public void subscriptionIsCreated() throws Exception {
        LOGGER.debug("Creating a subscription.");
        JsonNode subscription = eventManager.getJSONFromFile(SUBSCRIPTION_PATH);
        String notificationMeta = subscription.get("notificationMeta").asText();
        notificationMeta = notificationMeta.replace("{port}", mockServerPort);
        ((ObjectNode) subscription).put("notificationMeta", notificationMeta);
        subscriptionName = subscription.get("subscriptionName").asText();

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
        List<String> eventsIdList = eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH,
                eventNamesToSend);
        List<String> missingEventIds = dbManager.verifyEventsInDB(eventsIdList, 0);
        String errorMessage = "The following events are missing in mongoDB: "
                + missingEventIds.toString();
        assertEquals(errorMessage, 0, missingEventIds.size());
    }

    @Then("^the password should be encrypted in the database$")
    public void passwordShouldBeEncrypted() {
        LOGGER.debug("Verifying encoded password in subscription.");
        String json = dbManager.getSubscription(subscriptionName);
        JSONObject subscription = new JSONObject(json);
        String password = subscription.getString("password");
        assertTrue(EncryptionFormatter.isEncrypted(password));
    }

    @Then("^the subscription should trigger$")
    public void subscriptionShouldTrigger() {
        LOGGER.info("Verifying that subscription was triggered");
        clientAndServer.verify(request().withPath(MOCK_ENDPOINT).withBody(""),
                VerificationTimes.atLeast(1));
    }

    @Then("^the notification should be sent with a decrypted password in base64$")
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
        String endpoint = SUBSCRIPTION_GET_ENDPOINT.replaceAll("<name>", subscriptionName);
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
