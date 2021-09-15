package com.ericsson.ei.notifications.ttl;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoConstants;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.notifications.InformSubscriber;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "failed.notifications.collection.ttl: 1",
        "aggregations.collection.ttl: 1",
        "notification.retry: 1",
        "spring.data.mongodb.database: TestTTLSteps",
        "failed.notifications.collection.name: TestTTLSteps-failedNotifications",
        "rabbitmq.exchange.name: TestTTLSteps-exchange",
        "rabbitmq.consumer.name: TestTTLStepsConsumer"})
public class TestTTLSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTTLSteps.class);
    private static final String BASE_URL = "localhost";
    private static final String INVALID_ENDPOINT = "/invalid-endpoint";
    private static final String SUBSCRIPTION_NAME = "Subscription_1";

    private static final String SUBSCRIPTION_NAME_3 = "Subscription_Test_3";

    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();
    private HttpRequest httpRequest;

    private static final String SUBSCRIPTION_FILE_PATH = "src/functionaltests/resources/SubscriptionObject.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/AggregatedObject.json";
    private static final String SUBSCRIPTION_FILE_PATH_CREATION = "src/functionaltests/resources/subscription_single_ttlTest.json";
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";

    private static final long MAX_WAIT_TIME = 120000;

    private static JsonNode subscriptionObject;

    private MockServerClient mockServerClient;
    private ClientAndServer clientAndServer;

    @Value("${failed.notifications.collection.name}")
    private String failedNotificationCollection;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${aggregations.collection.name}")
    private String collection;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private InformSubscriber informSubscriber;

    @Autowired
    private SubscriptionHandler subscriptionHandler;

    @Before("@TestNotificationRetries")
    public void beforeScenario() {
        setUpMockServer();
    }

    @After("@TestNotificationRetries")
    public void afterScenario() throws IOException {
        LOGGER.debug("Shutting down mock servers.");
        mockServerClient.close();
        clientAndServer.stop();
    }

    @Given("^Subscription is created$")
    public void create_subscription_object() throws IOException, JSONException, MongoDBConnectionException {

        LOGGER.debug("Starting scenario @TestNotificationRetries.");
        mongoDBHandler.dropCollection(database, failedNotificationCollection);

        String subscriptionStr = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "utf-8");

        // replace with port of running mock server
        subscriptionStr = subscriptionStr.replaceAll("\\{port\\}", String.valueOf(clientAndServer.getPort()));

        subscriptionObject = new ObjectMapper().readTree(subscriptionStr);
        mongoDBHandler.createTTLIndex(database, failedNotificationCollection, MongoConstants.TIME, 1);
        assertEquals(false, subscriptionObject.get("notificationMeta").toString().contains("{port}"));
    }

    @When("^I want to inform subscriber$")
    public void inform_subscriber() throws IOException, AuthenticationException, MongoDBConnectionException {
        JsonNode aggregatedObject = eventManager.getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH);
        informSubscriber.informSubscriber(aggregatedObject.toString(), subscriptionObject);
    }

    @Then("^Verify that request has been retried")
    public void verify_request_has_been_made() throws JSONException {

        String retrievedRequests = mockServerClient.retrieveRecordedRequests(request().withPath(INVALID_ENDPOINT), Format.JSON);
        JSONArray requests = new JSONArray(retrievedRequests);

        // received requests include number of retries
        assertEquals(2, requests.length());
    }

    @Then("^Check failed notification is in database$")
    public void check_failed_notification_is_in_database() {
        final MongoCondition condition = MongoCondition.subscriptionNameCondition(SUBSCRIPTION_NAME);
        List<String> result = mongoDBHandler.find(database,
                failedNotificationCollection, condition);

        assertEquals(1, result.size());
        assertEquals("Could not find a failed notification matching the condition: " + condition,
                "\"" + SUBSCRIPTION_NAME + "\"", dbManager.getValueFromQuery(result, "subscriptionName", 0));
    }

    @Given("^A subscription is created using \"([^\"]*)\" with non-working notification meta$")
    public void a_subscription_is_created_at_the_end_point_with_non_existent_notification_meta(String endPoint)
            throws Throwable {
        String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH_CREATION), "UTF-8");
        JSONArray jsonArr = new JSONArray(readFileToString);
        httpRequest = new HttpRequest(HttpMethod.POST);
        httpRequest.setHost(hostName)
                   .setPort(applicationPort)
                   .setEndpoint(endPoint)
                   .addHeader("content-type", "application/json")
                   .addHeader("Accept", "application/json")
                   .setBody(jsonArr.toString());
        httpRequest.performRequest();
    }

    @Given("^I send an Eiffel event$")
    public void eiffel_events_are_sent() throws Throwable {
        LOGGER.debug("Sending an Eiffel event");
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
        List<String> missingEventIds = dbManager.verifyEventsInDB(
                eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend), 0);
        assertEquals("The following events are missing in mongoDB: " + missingEventIds.toString(), 0,
                missingEventIds.size());
        LOGGER.debug("Eiffel event is sent");
    }

    @When("^Aggregated object is created$")
    public void aggregated_object_is_created() throws Throwable {
        // verify that aggregated object is created and present in db
        LOGGER.debug("Checking presence of aggregated Object");
        List<String> allObjects = mongoDBHandler.getAllDocuments(database, collection);
        String id = eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, getEventNamesToSend()).get(0);
        subscriptionHandler.checkSubscriptionForObject(allObjects.get(0), id);
        assertEquals(1, allObjects.size());
    }

    @When("^Failed notification is created$")
    public void a_failed_notification_is_created() throws Throwable {
        // verifying that failed notification is created and present in db
        int expectedSize = 1;
        final MongoCondition condition = MongoCondition.subscriptionNameCondition(SUBSCRIPTION_NAME_3);

        LOGGER.debug("Checking presence of failed notification in db");
        int notificationExistSize = getNotificationForExpectedSize(expectedSize, condition);
        assertEquals(expectedSize, notificationExistSize);
    }

    @Then("^Notification document should be deleted from the database$")
    public void the_Notification_document_should_be_deleted_from_the_database() throws Throwable {
        int expectedSize = 0;
        final MongoCondition condition = MongoCondition.subscriptionNameCondition(SUBSCRIPTION_NAME_3);
        LOGGER.debug("Checking deletion of notification document in db");

        int notificationExistSize = getNotificationForExpectedSize(expectedSize, condition);
        assertEquals(expectedSize, notificationExistSize);
    }

    @Then("^Aggregated Object document should be deleted from the database$")
    public void the_Aggregated_Object_document_should_be_deleted_from_the_database() throws Throwable {
        LOGGER.debug("Checking deletion of aggregated object in db");
        List<String> allObjects = null;
        // To be sure at least one minute has passed since creation of aggregated object
        TimeUnit.MINUTES.sleep(1);
        allObjects = mongoDBHandler.getAllDocuments(database, collection);
        assertEquals("Database is not empty.", true, allObjects.isEmpty());
    }

    /**
     * Setting up mock server to receive calls on one endpoint and respond with 500
     * to trigger retries of POST request
     */
    private void setUpMockServer() {
        int port = SocketUtils.findAvailableTcpPort();
        clientAndServer = ClientAndServer.startClientAndServer(port);
        LOGGER.debug("Setting up mockServerClient with port " + port);
        mockServerClient = new MockServerClient(BASE_URL, port);

        // set up expectations on mock server to get calls on this endpoint
        mockServerClient.when(request().withMethod("POST").withPath(INVALID_ENDPOINT))
                        .respond(HttpResponse.response().withStatusCode(500));
    }

    /**
     * Events used in the aggregation.
     */
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        return eventNames;
    }

    private int getNotificationForExpectedSize(int expectedSize, MongoCondition condition) {
        long maxTime = System.currentTimeMillis() + MAX_WAIT_TIME;
        List<String> notificationExist = null;

        while (System.currentTimeMillis() < maxTime) {
            notificationExist = mongoDBHandler.find(database,
                    failedNotificationCollection,
                    condition);

            if (notificationExist.size() == expectedSize) {
                return notificationExist.size();
            }
        }
        return notificationExist.size();
    }
}
