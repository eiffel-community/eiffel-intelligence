package com.ericsson.ei.notifications.ttl;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.subscriptionhandler.InformSubscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

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
    private static String subscriptionObject;
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/functionaltests/resources/AggregatedObject.json";

    private MockServerClient mockServerClient;
    private ClientAndServer clientAndServer;

    // setting up mockserver which will record number of requests with these values in
    // notification meta in missed notification object
    private static final String BASE_URL = "https://localhost:";
    private static final String ENDPOINT = "/missed_notification";

    @LocalServerPort
    private int port; // get random application port

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


    @Before("TestAggregatedObject")
    private void beforeScenario() {
        // set up mock server, which is recording requests every time a
        // missed notification with meta data tries.
        setUpMockServer();

        clientAndServer = ClientAndServer.startClientAndServer(port);
    }

    @After("@TestSubscription")
    private void afterScenario() throws IOException {
        // close down mockServerClient here
        System.out.println("Shutting down both mockServerClient and clientAndServer");
        mockServerClient.close();
        clientAndServer.stop();
    }

    private void setUpMockServer() {
        System.out.println("Setting up mockServerClient with port " + port);
        mockServerClient = new MockServerClient(BASE_URL, port);
        // set up expectations on mock server
        mockServerClient.when(request().withMethod("POST").withPath(ENDPOINT))
                .respond(HttpResponse.response().withStatusCode(200)); // might change to status_ok?
    }

    /**
     * Retrieve number of requests made to mock server
     * @return receivedRequests
     *      an array of http requests made to mock server
     * */
    private HttpRequest[] retrieveRequests() {
        // retrieveRecordedRequests from mock server
        // use to compare with in assert

        HttpRequest[] receivedRequests = {};
        receivedRequests = mockServerClient.retrieveRecordedRequests(request().withPath(ENDPOINT));
        System.out.println("Received requests: " + receivedRequests.length);
        return receivedRequests;
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

        //update notificationmeta with url + port for mockserver
        // and randomly generated port - reuse functional test base class method?
        document.put("notificationMeta", BASE_URL + port + ENDPOINT);
        System.out.println("notification meta: " + document.get("notificationMeta").toString());

        System.out.println("Document is ready " + document.toString());

        return document;
    }

    // START TEST SCENARIOS

    @Given("^\"(.*)\" is prepared with index \"([A-Za-z0-9_]+)\"$")
    public void prepare_with_index(String fileName, String indexName) {

        try {
            fileContent = FileUtils.readFileToString(new File(fileName), "utf-8");
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Preparing object with a new fieldname to be used for index
        dbObject = prepareDocument(fileContent, indexName);
    }

    @And("^\"([A-Za-z0-9_]+)\" is created in database \"([A-Za-z0-9_]+)\" with index \"([A-Za-z0-9_]+)\"$")
    public void objects_are_created_in_database(String collection, String database, String indexName) {
        // setting 1 second TTL on index in db
        mongoDBHandler.createTTLIndex(database, collection, indexName, 1);

        Boolean isInserted = mongoDBHandler.insertDocument(database, collection, dbObject.toString());
        assertEquals(true, isInserted);

        result = mongoDBHandler.getAllDocuments(database, collection);
        assertEquals(false, result.isEmpty());
    }

    @When("^I sleep for \"([^\"]*)\" seconds")
    public void i_sleep_for_time(int sleepTime) throws Throwable {
        System.out.println("Sleeping for " + sleepTime + " at " + LocalTime.now());
        System.out.println("TTL for notification is " + missedNotificationTTL);
        System.out.println("TTL for aggregated object is " + aggregatedObjectTTL);

        // The background task deleting notifications from Mongo db runs every 60 seconds,
        // so 60 seconds sleep is needed for overhead
        TimeUnit.SECONDS.sleep(sleepTime);

        System.out.println("Woke up at " + LocalTime.now());
    }

    @Then("^\"([^\"]*)\" has been deleted from \"([A-Za-z0-9_]+)\" database$")
    public void has_been_deleted_from_database(String collection, String database) {
        result = mongoDBHandler.getAllDocuments(database, collection);
        assertEquals("[]", result.toString());
    }

    @And("^Verify that request has been made several times$")
    public void verify_request_has_been_made() {
        // check mockserver from here
        System.out.println("Checking mock server ...");
        // TODO: null pointer exception here?
        //HttpRequest[] retrievedRequests = mockServerClient.retrieveRecordedRequests(request());
        //System.out.println("Mock server client " + mockServerClient.toString());
        //System.out.println("Received requests " + retrievedRequests.length);

    }


    // TODO: Test using Inform subscription instead
    // SCENARIO @TestSubscription
    @Given("^Subscription is created$")
    public void create_subscription() {
        try {
            subscriptionObject = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "utf-8");
        } catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @When("^Notification is triggered$")
    public void trigger_notification() {
        // Trigger post request to mock server with subscription?
        try {
            informSubscription.informSubscriber(AGGREGATED_OBJECT_FILE_PATH, new ObjectMapper().readTree(subscriptionObject));
            System.out.println("Informing subscriber...");
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }



}
