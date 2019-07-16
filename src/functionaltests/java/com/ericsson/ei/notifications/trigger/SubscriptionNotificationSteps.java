package com.ericsson.ei.notifications.trigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ericsson.ei.handlers.MongoDBHandler;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.SocketUtils;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
public class SubscriptionNotificationSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        SubscriptionNotificationSteps.class);

    private static final String SUBSCRIPTION_WITH_JSON_PATH = "src/functionaltests/resources/subscription_multiple.json";
    private static final String SUBSCRIPTION_WITH_BAD_NOTIFICATION = "src/functionaltests/resources/subscription_fail_notifications.json";
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";
    private static final String REST_ENDPOINT = "/rest";
    private static final String REST_ENDPOINT_AUTH = "/rest/with/auth";
    private static final String REST_ENDPOINT_PARAMS = "/rest/with/params";
    private static final String REST_ENDPOINT_AUTH_PARAMS = "/rest/with/auth/params";
    private static final String REST_ENDPOINT_RAW_BODY = "/rest/rawBody";
    private static final String REST_ENDPOINT_BAD = "/rest/bad";

    @LocalServerPort
    private int applicationPort;

    @Value("${email.sender}")
    private String sender;

    @Value("${aggregated.collection.name}")
    private String aggregatedCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabase;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollection;

    @Value("${subscription.collection.name}")
    private String subscriptionCollection;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private SimpleSmtpServer smtpServer;
    private ClientAndServer restServer;
    private MockServerClient mockClient;
    private ResponseEntity response;

    @After()
    public void afterScenario() throws IOException {
        LOGGER.debug("Stopping SMTP and REST Mock Servers");
        if (smtpServer != null) {
            smtpServer.stop();
        }
        restServer.stop();
        mockClient.close();

        mongoDBHandler.dropCollection(missedNotificationDatabase, missedNotificationCollection);
        mongoDBHandler.dropDatabase(missedNotificationDatabase);
        mongoDBHandler.dropCollection(database, subscriptionCollection);
        mongoDBHandler.dropCollection(database, aggregatedCollectionName);
        mongoDBHandler.dropDatabase(database);
    }

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) throws Exception {
        System.out.println("Setting up REST endpoints");
        setupRestEndpoints();

        HttpRequest getRequest = new HttpRequest(HttpRequest.HttpMethod.GET);
        response = getRequest.setHost(getHostName())
                             .setPort(applicationPort)
                             .addHeader("content-type", "application/json")
                             .addHeader("Accept", "application/json")
                             .setEndpoint(endPoint)
                             .performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @Given("mail server is up")
    public void mail_server_is_up() throws IOException {
        System.out.println("Setting up mail server");
        setupSMTPServer();
    }

    @Given("^Subscriptions are setup using REST API \"([^\"]*)\"$")
    public void subscriptions_are_setup_using_REST_API(String endPoint) throws Throwable {
        String jsonDataAsString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_JSON_PATH), "UTF-8");
        jsonDataAsString = stringReplaceText(jsonDataAsString);

        List<String> subscriptionNames = readSubscriptionNames(jsonDataAsString);

        postSubscriptions(jsonDataAsString, endPoint);
        validateSubscriptionsSuccessfullyAdded(endPoint, subscriptionNames);
    }

    @Given("Bad subscriptions are created using \"([^\"]*)\"")
    public void subscription_are_created_using(String endPoint) throws Exception {
        String jsonDataAsString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_BAD_NOTIFICATION), "UTF-8");
        jsonDataAsString = stringReplaceText(jsonDataAsString);

        List<String> subscriptionNames = readSubscriptionNames(jsonDataAsString);

        postSubscriptions(jsonDataAsString, endPoint);
        validateSubscriptionsSuccessfullyAdded(endPoint, subscriptionNames);
    }

    @When("^I send Eiffel events$")
    public void send_eiffel_events() throws Throwable {
        LOGGER.debug("About to send Eiffel events.");
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
        List<String> missingEventIds = dbManager.verifyEventsInDB(
                eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend));
        assertEquals("The following events are missing in mongoDB: " + missingEventIds.toString(), 0,
                missingEventIds.size());
        LOGGER.debug("Eiffel events sent.");
    }

    @When("^Wait for EI to aggregate objects")
    public void wait_for_ei_to_aggregate_objects() throws Throwable {
        List<String> eventNamesToSend = getEventNamesToSend();
        LOGGER.debug("Checking Aggregated Objects.");
        List<String> arguments = new ArrayList<>(
                eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend));
        arguments.add("id=TC5");
        arguments.add("conclusion=SUCCESSFUL");
        List<String> missingArguments = dbManager.verifyAggregatedObjectInDB(arguments);
        assertEquals("The following arguments are missing in the Aggregated Object in mongoDB: "
                + missingArguments.toString(), 0, missingArguments.size());
    }

    @When("^I send one previous event again$")
    public void send_one_previous_event() throws Throwable {
        List<String> eventNamesToSend = new ArrayList<>();
        eventNamesToSend.add("event_EiffelArtifactCreatedEvent_3");
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
    }

    @Then("^Mail subscriptions were triggered$")
    public void mail_subscriptions_were_triggered() {
        LOGGER.debug("Verifying received emails.");
        List<SmtpMessage> emails = smtpServer.getReceivedEmails();
        assert (emails.size() > 0);

        for (SmtpMessage email : emails) {
            // assert correct sender.
            assertEquals(email.getHeaderValue("From"), sender);
            // assert given test case exist in body.
            assert (email.getBody().contains("TC5"));
        }
    }

    @Then("^Rest subscriptions were triggered$")
    public void check_rest_subscriptions_were_triggered() throws Throwable {
        restSubscriptionsTriggered(1);
        // clean mock server information of requests
        mockClient.reset();
    }

    @Then("Missed notifications should exist in the database")
    public void missed_notifications_should_exist_in_database() {
        String expectedRestPostNotification = "Subscription_bad_notification_rest_endpoint";
        String expectedMailNotification = "Subscription_bad_mail";

        String condition =
            "{$or:[{\"subscriptionName\": \"" + expectedRestPostNotification + "\"}"
            + "{\"subscriptionName\": \"" + expectedMailNotification + "\"}]}";

        List<String> result = mongoDBHandler.find(missedNotificationDatabase,
            missedNotificationCollection, condition);

        assertEquals(2, result.size());
    }

    @Then("^No subscription is retriggered$")
    public void no_subscription_is_retriggered() throws Throwable {
        restSubscriptionsTriggered(0);
    }

    private void restSubscriptionsTriggered(int times) throws Throwable {
        LOGGER.debug("Verifying REST requests.");
        List<String> endpointsToCheck = new ArrayList<>(Arrays.asList(REST_ENDPOINT, REST_ENDPOINT_AUTH,
                REST_ENDPOINT_PARAMS, REST_ENDPOINT_AUTH_PARAMS,
            REST_ENDPOINT_RAW_BODY));

        assert (allEndpointsGotAtLeastXCalls(endpointsToCheck, times));
        if (times > 0) {
            for (String endpoint : endpointsToCheck) {
                assert (requestBodyContainsStatedValues(endpoint));
            }
        }
    }

    /**
     * Assemble subscription names in a list.
     *
     * @param jsonDataAsString JSON string containing subscriptions
     */
    private List<String> readSubscriptionNames(String jsonDataAsString) {
        List<String> subscriptionNames = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonDataAsString);
        for (int i = 0; i < jsonArray.length(); i++) {
            subscriptionNames.add(jsonArray.getJSONObject(i).get("subscriptionName").toString());
        }
        return subscriptionNames;
    }

    /**
     * POST subscriptions to endpoint.
     *
     * @param jsonDataAsString JSON string containing subscriptions
     * @param endPoint         endpoint to use in POST
     * @throws Exception
     */
    private void postSubscriptions(String jsonDataAsString, String endPoint) throws Exception {
        HttpRequest postRequest = new HttpRequest(HttpRequest.HttpMethod.POST);
        response = postRequest.setHost(getHostName())
                              .setPort(applicationPort)
                              .addHeader("content-type", "application/json")
                              .addHeader("Accept", "application/json")
                              .setEndpoint(endPoint)
                              .setBody(jsonDataAsString)
                              .performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    /**
     * Verify that subscriptions were successfully posted.
     *
     * @param endPoint endpoint to use in GET
     * @throws Exception
     */
    private void validateSubscriptionsSuccessfullyAdded(String endPoint, List<String> subscriptionNames) throws Exception {
        HttpRequest getRequest = new HttpRequest(HttpRequest.HttpMethod.GET);
        response = getRequest.setHost(getHostName())
                             .setPort(applicationPort)
                             .addHeader("content-type", "application/json")
                             .addHeader("Accept", "application/json")
                             .setEndpoint(endPoint)
                             .performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        LOGGER.debug("Checking that response contains all subscriptions");
        for (String subscriptionName : subscriptionNames) {
            assertTrue(response.toString().contains(subscriptionName));
        }
    }

    /**
     * Checks that an endpoint got at least the number of calls as expected.
     *
     * @param endpoints     List of endpoints to check.
     * @param expectedCalls Integer with the least number of calls.
     * @return true if all endpoints had at least the number of calls as
     * expected.
     * @throws JSONException
     * @throws InterruptedException
     */
    private boolean allEndpointsGotAtLeastXCalls(final List<String> endpoints, int expectedCalls)
            throws JSONException, InterruptedException {
        List<String> endpointsToCheck = new ArrayList<>(endpoints);

        long stopTime = System.currentTimeMillis() + 30000;
        while (!endpointsToCheck.isEmpty() && stopTime > System.currentTimeMillis()) {
            for (String endpoint : endpoints) {
                String restBodyData = mockClient.retrieveRecordedRequests(request().withPath(endpoint), Format.JSON);
                int actualRestCalls = new JSONArray(restBodyData).length();
                if (actualRestCalls >= expectedCalls) {
                    endpointsToCheck.remove(endpoint);
                }
            }
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        return endpointsToCheck.isEmpty();
    }

    /**
     * Verify that request made to endpoint contains the correct information.
     *
     * @param endpoint endpoint to check
     * @return true if verification was successful, false otherwise
     * @throws JSONException
     */
    private boolean requestBodyContainsStatedValues(String endpoint) throws JSONException {
        int tc5 = 0, successfull = 0;
        String restBodyData = mockClient.retrieveRecordedRequests(request().withPath(endpoint), Format.JSON);
        if (restBodyData == null) {
            LOGGER.error("No calls made to rest endpoint '" + endpoint + "'.");
            return false;
        }
        JSONArray jsonArray = new JSONArray(restBodyData);

        for (int i = 0; i < jsonArray.length(); i++) {
            String requestBody = jsonArray.get(i).toString();
            if (requestBody.contains("TC5")) {
                tc5++;
            }
            if (requestBody.contains("SUCCESSFUL")) {
                successfull++;
            }
        }
        return (tc5 > 0 && successfull > 0);
    }

    /**
     * Setting up the needed endpoints for the functional test.
     */
    private void setupRestEndpoints() {
        int port = SocketUtils.findAvailableTcpPort();
        restServer = startClientAndServer(port);

        LOGGER.debug("Setting up endpoints on host '" + getHostName() + "' and port '" + port + "'.");
        mockClient = new MockServerClient(getHostName(), port);
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT))
                  .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST")
                                 .withPath(REST_ENDPOINT_AUTH)
                                 .withHeader("Authorization",
                                     "Basic bXlVc2VyTmFtZTpteVBhc3N3b3Jk"))
                  .respond(response().withStatusCode(201));
        mockClient.when(
            request().withMethod("POST").withPath(REST_ENDPOINT_PARAMS))
                  .respond(response().withStatusCode(201));
        mockClient.when(
            request().withMethod("POST").withPath(REST_ENDPOINT_AUTH_PARAMS))
                  .respond(response().withStatusCode(201));
        mockClient.when(
            request().withMethod("POST").withPath(REST_ENDPOINT_RAW_BODY))
                  .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(
            REST_ENDPOINT_BAD))
                  .respond(response().withStatusCode(401));
    }

    /**
     * Setup and start SMTP mock server.
     *
     * @throws IOException
     */
    private void setupSMTPServer() throws IOException {
        boolean connected = false;
        while (!connected) {
            try {
                int port = SocketUtils.findAvailableTcpPort();
                LOGGER.debug("Setting SMTP port to " + port);
                mailSender.setPort(port);
                smtpServer = SimpleSmtpServer.start(port);
                // connected, go on
                connected = true;
            } catch (BindException e) {
                String msg = "Failed to start SMTP server. address already in use. Try again!";
                LOGGER.debug(msg, e);
            }
        }
    }

    /**
     * Events used in the aggregation.
     */
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");
        return eventNames;
    }

    /**
     * Replaces tags in the subscription JSON string with valid information.
     *
     * @param text JSON string containing replaceable tags
     * @return Processed content
     */
    private String stringReplaceText(String text) {
        text = text.replaceAll("\\$\\{rest\\.host\\}", "localhost");
        text = text.replaceAll("\\$\\{rest\\.port\\}", String.valueOf(restServer.getLocalPort()));
        text = text.replaceAll("\\$\\{rest\\.raw.body\\}",
            REST_ENDPOINT_RAW_BODY);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\}", REST_ENDPOINT);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\}", REST_ENDPOINT_AUTH);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.params\\}", REST_ENDPOINT_PARAMS);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\.params\\}", REST_ENDPOINT_AUTH_PARAMS);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.bad\\}",
            REST_ENDPOINT_BAD);
        return text;
    }
}