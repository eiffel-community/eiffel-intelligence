package com.ericsson.ei.notifications.trigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.ericsson.ei.handlers.MongoCondition;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = { "spring.data.mongodb.database: SubscriptionNotificationSteps",
        "failed.notification.database-name: SubscriptionNotificationSteps-failedNotifications",
        "rabbitmq.exchange.name: SubscriptionNotificationSteps-exchange",
        "rabbitmq.consumerName: SubscriptionNotificationSteps-consumer" })
public class SubscriptionNotificationSteps extends FunctionalTestBase {

    private static final Logger LOGGER = getLogger(SubscriptionNotificationSteps.class);

    private static final String SUBSCRIPTION_WITH_JSON_PATH = "src/functionaltests/resources/subscription_multiple.json";
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";
    private static final String REST_ENDPOINT = "/rest";
    private static final String REST_ENDPOINT_AUTH = "/rest/with/auth";
    private static final String REST_ENDPOINT_PARAMS = "/rest/with/params";
    private static final String REST_ENDPOINT_AUTH_PARAMS = "/rest/with/auth/params";
    private static final String REST_ENDPOINT_RAW_BODY = "/rest/rawBody";
    private static final String REST_ENDPOINT_BAD = "/rest/bad";
    private static final String EI_SUBSCRIPTIONS_ENDPOINT = "/subscriptions";

    @LocalServerPort
    private int applicationPort;

    @Value("${email.sender}")
    private String sender;

    @Value("${aggregated.collection.name}")
    private String aggregatedCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${failed.notification.database-name}")
    private String failedNotificationDatabase;

    @Value("${failed.notification.collection-name}")
    private String failedNotificationCollection;

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

    @Before()
    public void beforeScenario() {
        mongoDBHandler.dropDatabase(database);
        mongoDBHandler.dropDatabase(failedNotificationDatabase);
    }

    @After()
    public void afterScenario() {
        LOGGER.debug("Stopping SMTP and REST Mock Servers");
        if (smtpServer != null) {
            smtpServer.stop();
        }
        restServer.stop();
        mockClient.close();
    }

    @Given("^The REST API is up and running$")
    public void the_REST_API_is_up_and_running() throws Exception {
        LOGGER.debug("Setting up REST endpoints");
        setupRestEndpoints();

        HttpRequest getRequest = new HttpRequest(HttpRequest.HttpMethod.GET);
        response = getRequest.setHost(getHostName()).setPort(applicationPort)
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json")
                .setEndpoint(EI_SUBSCRIPTIONS_ENDPOINT).performRequest();
        assertEquals("EI rest API status code: ", HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @Given("Mail server is up")
    public void mail_server_is_up() throws IOException {
        LOGGER.debug("Setting up mail server");
        setupSMTPServer();
    }

    @Given("Subscriptions with bad notification meta are created")
    public void create_subscriptions_with_bad_notification_meta() throws Exception {
        List<String> subscriptionNames = new ArrayList<>();
        subscriptionNames.add("Subscription_bad_mail");
        subscriptionNames.add("Subscription_bad_notification_rest_endpoint");

        createSubscriptions(subscriptionNames);
    }

    @Given("Subscriptions are created")
    public void subscriptions_are_created() throws Throwable {
        List<String> subscriptionNames = new ArrayList<>();
        subscriptionNames.add("Subscription_Mail");
        subscriptionNames.add("Subscription_Rest_Params_in_Head");
        subscriptionNames.add("Subscription_Rest_Auth_Params_in_Head");
        subscriptionNames.add("Subscription_Rest_Params_in_Url");
        subscriptionNames.add("Subscription_Rest_Auth_Params_in_Url");
        subscriptionNames.add("Subscription_Raw_Body");

        createSubscriptions(subscriptionNames);
    }

    @When("^I send Eiffel events$")
    public void send_eiffel_events() throws Throwable {
        LOGGER.debug("About to send Eiffel events.");
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
        List<String> missingEventIds = dbManager
                .verifyEventsInDB(eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend), 0);
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
            assertEquals("Assert correct email sender: ", email.getHeaderValue("From"), sender);
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

    @Then("Missed notification db should contain (\\d+) objects")
    public void missed_notification_db_should_contain_x_objects(int maxObjectsInDB) throws Throwable {
        int minWaitTime = 5;
        int maxWaittime = 20;

        MongoCondition condition = MongoCondition.emptyCondition();
        int missedNotifications = getDbSizeForCondition(minWaitTime, maxWaittime, maxObjectsInDB, condition);

        assertEquals(missedNotifications, maxObjectsInDB);
        assertEquals("Number of missed notifications saved in the database: " + missedNotifications, maxObjectsInDB,
                missedNotifications);
    }

    @Then("^No subscription is retriggered$")
    public void no_subscription_is_retriggered() throws Throwable {
        restSubscriptionsTriggered(0);
    }

    private void restSubscriptionsTriggered(int times) throws Throwable {
        LOGGER.debug("Verifying REST requests.");
        List<String> endpointsToCheck = new ArrayList<>(Arrays.asList(REST_ENDPOINT, REST_ENDPOINT_AUTH,
                REST_ENDPOINT_PARAMS, REST_ENDPOINT_AUTH_PARAMS, REST_ENDPOINT_RAW_BODY));

        assert (allEndpointsGotAtLeastXCalls(endpointsToCheck, times));
        if (times > 0) {
            for (String endpoint : endpointsToCheck) {
                assert (requestBodyContainsStatedValues(endpoint));
            }
        }
    }

    /**
     * Creating subscriptions defined in a JSON file, given the list of subscription
     * names
     */
    private void createSubscriptions(List<String> subscriptionNames) throws Exception {
        JsonNode subscriptions = eventManager.getJSONFromFile(SUBSCRIPTION_WITH_JSON_PATH);

        List<String> subscriptionsToSend = new ArrayList<>();

        for (String subscriptionName : subscriptionNames) {
            JsonNode subscription = subscriptions.get(subscriptionName);
            String subscriptionString = replaceTagsInNotificationMeta(subscription.toString());
            subscriptionsToSend.add(subscriptionString);
        }
        postSubscriptions(subscriptionsToSend.toString());
        validateSubscriptionsSuccessfullyAdded(subscriptionNames);
    }

    /**
     * POST subscriptions to EI /subscriptions endpoint.
     *
     * @param jsonDataAsString
     *            JSON string containing subscriptions
     * @throws Exception
     */
    private void postSubscriptions(String jsonDataAsString) throws Exception {
        HttpRequest postRequest = new HttpRequest(HttpRequest.HttpMethod.POST);
        response = postRequest.setHost(getHostName()).setPort(applicationPort)
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json")
                .setEndpoint(EI_SUBSCRIPTIONS_ENDPOINT).setBody(jsonDataAsString).performRequest();
        assertEquals("Expected to add subscription to EI", HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    /**
     * Verify that subscriptions were successfully posted.
     *
     * @param subscriptionNames
     *            A list containing subscription names to check
     * @throws Exception
     */
    private void validateSubscriptionsSuccessfullyAdded(List<String> subscriptionNames) throws Exception {
        HttpRequest getRequest = new HttpRequest(HttpRequest.HttpMethod.GET);
        response = getRequest.setHost(getHostName()).setPort(applicationPort)
                .addHeader("content-type", "application/json").addHeader("Accept", "application/json")
                .setEndpoint(EI_SUBSCRIPTIONS_ENDPOINT).performRequest();
        assertEquals("Subscription successfully added in EI: ", HttpStatus.OK.value(), response.getStatusCodeValue());
        LOGGER.debug("Checking that response contains all subscriptions");
        for (String subscriptionName : subscriptionNames) {
            assertTrue(response.toString().contains(subscriptionName));
        }
    }

    /**
     * Checks that an endpoint got at least the number of calls as expected.
     *
     * @param endpoints
     *            List of endpoints to check.
     * @param expectedCalls
     *            Integer with the least number of calls.
     * @return true if all endpoints had at least the number of calls as expected.
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
     * @param endpoint
     *            endpoint to check
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
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT)).respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_AUTH).withHeader("Authorization",
                "Basic bXlVc2VyTmFtZTpteVBhc3N3b3Jk")).respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_PARAMS))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_AUTH_PARAMS))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_RAW_BODY))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_BAD))
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
     * Replaces placeholder tags in notification meta in the subscription JSON
     * string with valid notification meta.
     *
     * @param text
     *            JSON string containing replaceable tags
     * @return Processed content
     */
    private String replaceTagsInNotificationMeta(String text) {
        text = text.replaceAll("\\$\\{rest\\.host\\}", "localhost");
        text = text.replaceAll("\\$\\{rest\\.port\\}", String.valueOf(restServer.getLocalPort()));
        text = text.replaceAll("\\$\\{rest\\.raw.body\\}", REST_ENDPOINT_RAW_BODY);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\}", REST_ENDPOINT);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\}", REST_ENDPOINT_AUTH);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.params\\}", REST_ENDPOINT_PARAMS);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\.params\\}", REST_ENDPOINT_AUTH_PARAMS);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.bad\\}", REST_ENDPOINT_BAD);
        return text;
    }

    /**
     * Returns the content size of a DB query after a minimum time and max time
     * unless expected size was reached after minimum time.
     *
     * @param minWaitTime
     *            Seconds to wait before checking first time
     * @param maxWaitTime
     *            Max seconds to wait to reach expected size
     * @param expectedSize
     *            Expected size
     * @param condition
     *            Condition
     * @return
     * @throws InterruptedException
     */
    private int getDbSizeForCondition(int minWaitTime, int maxWaitTime, int expectedSize, MongoCondition condition)
            throws InterruptedException {
        TimeUnit.SECONDS.sleep(minWaitTime);
        long maxTime = System.currentTimeMillis() + maxWaitTime;
        List<String> queryResult = null;

        while (System.currentTimeMillis() < maxTime) {
            queryResult = mongoDBHandler.find(failedNotificationDatabase,
                    failedNotificationCollection, condition);

            if (queryResult.size() == expectedSize) {
                return queryResult.size();
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("##########################################################");
        LOGGER.error("DB size did not match expected, Subsctiptions:\n{}", queryResult);
        return queryResult.size();
    }
}