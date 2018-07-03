package com.ericsson.ei.notifications.ttl;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.subscriptionhandler.InformSubscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.verify.VerificationTimes.exactly;

@Ignore
@TestPropertySource(properties = {"notification.ttl.value:1", "aggregated.collection.ttlValue:1", "notification.failAttempt:1"})
@ContextConfiguration(initializers = TestContextInitializer.class)
public class TestTTLSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTTLSteps.class);
    private static final String BASE_URL = "localhost";
    private static final String ENDPOINT = "/missed_notification";

    private static final String SUBSCRIPTION_FILE_PATH = "src/functionaltests/resources/SubscriptionObject.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/functionaltests/resources/AggregatedObject.json";
    private static final String MISSED_NOTIFICATION_FILE_PATH = "src/functionaltests/resources/MissedNotification.json";

    private static String subscriptionObject;
    private static String aggregatedObject;
    private static String missedNotification;

    private MockServerClient mockServerClient;
    private ClientAndServer clientAndServer;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabase;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${notification.failAttempt}")
    private String notificationFailAttempt;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private InformSubscription informSubscription;

    @Autowired
    private JmesPathInterface jmespath;

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

    // START TEST SCENARIOS

    @Given("^Missed notification is created in database with index \"([A-Za-z0-9_]+)\"$")
    public void missed_notification_is_created_in_database(String indexName) throws IOException {
        LOGGER.debug("Starting scenario @TestTTL");
        missedNotification = FileUtils.readFileToString(new File(MISSED_NOTIFICATION_FILE_PATH), "utf-8");
        BasicDBObject missedNotificationDocument = prepareDocumentWithIndex(missedNotification, indexName);

        // setting 1 second TTL on index in db
        mongoDBHandler.createTTLIndex(missedNotificationDatabase, missedNotificationCollectionName, indexName, 1);
        Boolean isInserted = mongoDBHandler.insertDocument(missedNotificationDatabase, missedNotificationCollectionName, missedNotificationDocument.toString());
        assertEquals(true, isInserted);

        List<String> result = mongoDBHandler.getAllDocuments(missedNotificationDatabase, missedNotificationCollectionName);
        assertEquals("Database " + missedNotificationDatabase + " is not empty.", false, result.isEmpty());
    }

    @Given("^Aggregated object is created in database with index \"([A-Za-z0-9_]+)\"$")
    public void aggregated_object_is_created_in_database(String indexName) throws IOException {
        aggregatedObject = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "utf-8");
        BasicDBObject aggregatedDocument = prepareDocumentWithIndex(aggregatedObject, indexName);

        // setting 1 second TTL on index in db
        mongoDBHandler.createTTLIndex(aggregationDataBaseName, aggregationCollectionName, indexName, 1);
        Boolean isInserted = mongoDBHandler.insertDocument(aggregationDataBaseName, aggregationCollectionName, aggregatedDocument.toString());
        assertEquals(true, isInserted);

        List<String> result = mongoDBHandler.getAllDocuments(aggregationDataBaseName, aggregationCollectionName);
        assertEquals("Database " + aggregationDataBaseName + " is not empty.", false, result.isEmpty());
    }

    @When("^I sleep for \"([^\"]*)\" seconds")
    public void i_sleep_for_time(int sleepTime) throws Throwable {
        LOGGER.debug("Sleeping for " + sleepTime + " at " + LocalTime.now());
        // The background task deleting notifications from Mongo DB
        // runs every 60 seconds
        TimeUnit.SECONDS.sleep(sleepTime);
    }


    @Then("^\"([^\"]*)\" has been deleted from \"([A-Za-z0-9_]+)\" database$")
    public void has_been_deleted_from_database(String collection, String database) {
        LOGGER.debug("Checking " + collection + " in " + database);
        List<String> result = mongoDBHandler.getAllDocuments(database, collection);
        assertEquals("[]", result.toString());
    }


    // SCENARIO @TestNotificationRetries

    @Given("^No missed notifications exists in database$")
    public void no_missed_notifications_exists_in_database() {
        mongoDBHandler.dropCollection(missedNotificationDatabase, missedNotificationCollectionName);
    }

    @Given("^Subscription is created$")
    public void create_subscription() throws IOException, JSONException {
        LOGGER.debug("Starting scenario @TestNotificationRetries.");
        subscriptionObject = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "utf-8");

        // replace with port of running mock server
        subscriptionObject = subscriptionObject.replaceAll("\\{port\\}", String.valueOf(clientAndServer.getPort()));
    }

    @When("^I fail to inform subscriber$")
    public void trigger_notification() throws IOException {
        aggregatedObject = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "utf-8");
        informSubscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionObject));
    }

    @Then("^Verify that request has been retried")
    public void verify_request_has_been_made() throws JSONException {
        String retrievedRequests = mockServerClient.retrieveRecordedRequests(request().withPath(ENDPOINT), Format.JSON);
        JSONArray requests = new JSONArray(retrievedRequests);
        LOGGER.debug("Requests made: " + requests.toString());

        // expect 1 retry of request
        assertEquals(2, requests.length());

        //TODO: wait?
        mockServerClient.verify(request().withPath(ENDPOINT), exactly(2));
    }

    @Then("^Missed notification is in database$")
    public void missed_notification_is_in_database() {
        List<String> result = mongoDBHandler.getAllDocuments(missedNotificationDatabase, missedNotificationCollectionName);
        assertEquals(1, result.size());
    }

    private void setUpMockServer() {
        int port = SocketUtils.findAvailableTcpPort();
        clientAndServer = ClientAndServer.startClientAndServer(port);
        LOGGER.debug("Setting up mockServerClient with port " + port);
        mockServerClient = new MockServerClient(BASE_URL, port);

        // set up expectations on mock server to get calls on this endpoint
        mockServerClient.when(request().withMethod("POST").withPath(ENDPOINT))
                .respond(HttpResponse.response().withStatusCode(500));
    }

    /**
     * Add a fieldname of date-type to be used as index in database
     * @param fileContent
     *      File containing JSON string
     * @param fieldName
     *      The name of the field to be inserted
     * @return
     *      A new BasicDBObject document
     * */
    private BasicDBObject prepareDocumentWithIndex(String fileContent, String fieldName) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = dateFormat.format(date);
        try {
            date = dateFormat.parse(time);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        BasicDBObject document = new BasicDBObject();
        document = document.parse(fileContent);
        document.put(fieldName, date);

        return document;
    }
}
