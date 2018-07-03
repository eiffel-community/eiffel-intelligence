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
import org.json.JSONObject;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.verify.VerificationTimes.exactly;

@Ignore
@TestPropertySource(properties = {"notification.ttl.value:1", "aggregated.collection.ttlValue:1", "notification.failAttempt:1"})
@ContextConfiguration(initializers = TestContextInitializer.class)
public class TestTTLSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTTLSteps.class);
    private static String fileContent;
    private ArrayList<String> result;
    private BasicDBObject dbObject;

    //TODO: trying with subscription file...
    private static final String SUBSCRIPTION_FILE_PATH = "src/functionaltests/resources/SubscriptionObject.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/functionaltests/resources/AggregatedObject.json";
    private static final String MISSED_NOTIFICATION_FILE_PATH = "src/functionaltests/resources/MissedNotification.json";
    private static String subscriptionObject;
    private static String aggregatedObject;
    private static String missedNotification;


    private MockServerClient mockServerClient;
    private ClientAndServer clientAndServer;

    // setting up mockserver which will receive requests with these values in
    // notification meta in missed notification object
    private static final String BASE_URL = "localhost";
    private static final String ENDPOINT = "/missed_notification";

    // TODO: remove the unneeded values from here? only keep ttl values + failattempt
    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabase;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${notification.ttl.value}")
    private String missedNotificationTTL;

    @Value("${notification.failAttempt}")
    private String notificationFailAttempt;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${aggregated.collection.ttlValue}")
    private String aggregatedObjectTTL;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private InformSubscription informSubscription;
    // inform subscription takes in values of failattempts and
    // ttl value from application properties

    @Autowired
    private JmesPathInterface jmespath; //why?

    @Before("@TestSubscription")
    public void beforeScenario() {
        setUpMockServer();
    }

    @After("@TestSubscription")
    public void afterScenario() throws IOException {
        LOGGER.debug("Shutting down mock servers.");
        mockServerClient.close();
        clientAndServer.stop();
    }

    // START TEST SCENARIOS

    @Given("^\"(.*)\" is prepared with index \"([A-Za-z0-9_]+)\"$")
    public void prepare_with_index(String fileName, String indexName) throws IOException {
        //fileContent = FileUtils.readFileToString(new File(fileName), "utf-8");
    }

    @Given("^\"([A-Za-z0-9_]+)\" is created in database$")
    public void objects_are_created_in_database(String indexName) throws IOException {
        // Preparing object with a new fieldname to be used for index

        aggregatedObject = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "utf-8");
        BasicDBObject aggregatedDocument = prepareDocument(aggregatedObject, indexName);

        // setting 1 second TTL on index in db
        mongoDBHandler.createTTLIndex(aggregationDataBaseName, aggregationCollectionName, indexName, 1);

        Boolean isInserted = mongoDBHandler.insertDocument(aggregationDataBaseName, aggregationCollectionName, aggregatedDocument.toString());
        assertEquals(true, isInserted);

        result = mongoDBHandler.getAllDocuments(aggregationDataBaseName, aggregationCollectionName);
        assertEquals("Database " + aggregationDataBaseName + " is not empty.", false, result.isEmpty());
        //TODO: make same for missed notification
    }

    @When("^I sleep for \"([^\"]*)\" seconds")
    public void i_sleep_for_time(int sleepTime) throws Throwable {
        LOGGER.debug("Sleeping for " + sleepTime + " at " + LocalTime.now());

        // The background task deleting notifications from Mongo db runs every 60 seconds,
        // so 60 seconds sleep is needed for overhead
        TimeUnit.SECONDS.sleep(sleepTime);

        LOGGER.debug("Woke up at " + LocalTime.now());
    }


    @Then("^\"([^\"]*)\" has been deleted from \"([A-Za-z0-9_]+)\" database$")
    public void has_been_deleted_from_database(String collection, String database) {
        result = mongoDBHandler.getAllDocuments(database, collection);
        assertEquals("[]", result.toString());
    }


    // TODO: Test using Inform subscription instead
    // check mock server has received calls per subscription made?
    // SCENARIO @TestSubscription


    @Given("^Subscription is created$")
    public void create_subscription() throws IOException, JSONException {
        LOGGER.debug("Creating subscription.");
        subscriptionObject = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "utf-8");

        // replace with port of running mock server
        subscriptionObject = subscriptionObject.replaceAll("\\{port\\}", String.valueOf(clientAndServer.getPort()));

        LOGGER.debug("Subscription " + subscriptionObject);
        LOGGER.debug("Notification meta: " + new JSONObject(subscriptionObject).getString("notificationMeta"));

    }

    @When("^Notification is triggered$")
    public void trigger_notification() throws IOException {
        // Function informSubscriber will perform a POST request with
        // the notificationmeta as url

        LOGGER.debug(subscriptionObject);
        informSubscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionObject));
    }

    @Then("^Verify that request has been made several times$")
    public void verify_request_has_been_made() throws JSONException {
        // check that mock server has received requests with 'notification' meta endpoint
        // exactly 1 time because failattempt is set to 1

        String retrievedRequests = mockServerClient.retrieveRecordedRequests(request().withPath(ENDPOINT), Format.JSON);
        JSONArray requests = new JSONArray(retrievedRequests);
        assertEquals(1, requests.length());

        //TODO: wait?
        mockServerClient.verify(request().withPath(ENDPOINT), exactly(1));

    }


    private void setUpMockServer() {
        int port = SocketUtils.findAvailableTcpPort();

        clientAndServer = ClientAndServer.startClientAndServer(port);

        LOGGER.debug("Setting up mockServerClient with port " + port);
        mockServerClient = new MockServerClient(BASE_URL, port);

        // set up expectations on mock server to get calls on this endpoint
        mockServerClient.when(request().withMethod("POST").withPath(ENDPOINT))
                .respond(HttpResponse.response().withStatusCode(200)); // change to status_ok?
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
    public BasicDBObject prepareDocument(String fileContent, String fieldName) {
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

        LOGGER.debug("notification meta: " + document.get("notificationMeta").toString());

        // LOGGER.debug("Document is ready " + document.toString());

        return document;
    }


}
