package com.ericsson.ei.notifications.ttl;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.subscriptionhandler.InformSubscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.TestContextInitializer;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

@Ignore
@TestPropertySource(properties = { "notification.ttl.value:1", "aggregated.collection.ttlValue:1",
		"notification.failAttempt:1" })
@ContextConfiguration(initializers = TestContextInitializer.class)
public class TestTTLSteps extends FunctionalTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTTLSteps.class);
	private static final String BASE_URL = "localhost";
	private static final String ENDPOINT = "/missed_notification";
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

	private static JsonNode subscriptionObject;

	private MockServerClient mockServerClient;
	private ClientAndServer clientAndServer;

	@Value("${missedNotificationDataBaseName}")
	private String missedNotificationDatabase;

	@Value("${missedNotificationCollectionName}")
	private String missedNotificationCollection;

	@Value("${spring.data.mongodb.database}")
	private String dataBase;

	@Value("${aggregated.collection.name}")
	private String collection;

	@Autowired
	private MongoDBHandler mongoDBHandler;

	@Autowired
	private InformSubscription informSubscription;

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
	public void create_subscription_object() throws IOException, JSONException {

		LOGGER.debug("Starting scenario @TestNotificationRetries.");
		mongoDBHandler.dropCollection(missedNotificationDatabase, missedNotificationCollection);

		String subscriptionStr = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "utf-8");

		// replace with port of running mock server
		subscriptionStr = subscriptionStr.replaceAll("\\{port\\}", String.valueOf(clientAndServer.getPort()));

		subscriptionObject = new ObjectMapper().readTree(subscriptionStr);
		assertEquals(false, subscriptionObject.get("notificationMeta").toString().contains("{port}"));
	}

	@When("^I want to inform subscriber$")
	public void inform_subscriber() throws IOException {
		JsonNode aggregatedObject = eventManager.getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH);
		informSubscription.informSubscriber(aggregatedObject.toString(), subscriptionObject);
	}

	@Then("^Verify that request has been retried")
	public void verify_request_has_been_made() throws JSONException {

		String retrievedRequests = mockServerClient.retrieveRecordedRequests(request().withPath(ENDPOINT), Format.JSON);
		JSONArray requests = new JSONArray(retrievedRequests);

		// received requests include number of retries
		assertEquals(2, requests.length());
	}

	@Then("^Check missed notification is in database$")
	public void check_missed_notification_is_in_database() {
		String condition = "{\"subscriptionName\" : \"" + SUBSCRIPTION_NAME + "\"}";
		List<String> result = mongoDBHandler.find(missedNotificationDatabase, missedNotificationCollection, condition);

		assertEquals(1, result.size());
		assertEquals("Could not find a missed notification matching the condition: " + condition,
				"\"" + SUBSCRIPTION_NAME + "\"", dbManager.getValueFromQuery(result, "subscriptionName", 0));
	}

	@Given("^A subscription is created  at the end point \"([^\"]*)\" with non-existent notification meta$")
	public void a_subscription_is_created_at_the_end_point_with_non_existent_notification_meta(String endPoint)
			throws Throwable {
		String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH_CREATION), "UTF-8");
		JSONArray jsonArr = new JSONArray(readFileToString);
		httpRequest = new HttpRequest(HttpMethod.POST);
		httpRequest.setHost(hostName).setPort(applicationPort).setEndpoint(endPoint)
				.addHeader("content-type", "application/json").addHeader("Accept", "application/json")
				.setBody(jsonArr.toString());
		httpRequest.performRequest();
	}

	@Given("^I send an Eiffel event and consequently aggregated object and thereafter missed notification is created$")
	public void i_send_an_Eiffel_event_and_consequently_aggregated_object_and_thereafter_missed_notification_is_created()
			throws Throwable {
		LOGGER.debug("Sending an Eiffel event");
		List<String> eventNamesToSend = getEventNamesToSend();
		eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
		List<String> missingEventIds = dbManager
				.verifyEventsInDB(eventManager.getEventsIdList(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend));
		assertEquals("The following events are missing in mongoDB: " + missingEventIds.toString(), 0,
				missingEventIds.size());
		LOGGER.debug("Eiffel event is sent");

		// verify that aggregated object is created and present in db
		LOGGER.debug("Checking presence of aggregated Object");
		List<String> allObjects = mongoDBHandler.getAllDocuments(dataBase, collection);
		assertEquals(1, allObjects.size());

		// verifying that missed notification is created and present in db
		String condition = "{\"subscriptionName\" : \"" + SUBSCRIPTION_NAME_3 + "\"}";
		long maxTime = System.currentTimeMillis() + 3000;
		List<String> notificationExit = null;
		LOGGER.debug("Checking presence of missnotification in db");
		while (System.currentTimeMillis() < maxTime) {
			notificationExit = mongoDBHandler.find(missedNotificationDatabase, missedNotificationCollection, condition);
			if (!notificationExit.isEmpty()) {
				break;
			}
		}
		assertEquals(1, notificationExit.size());
	}

	@Then("^the Notification document should be deleted from the database according to ttl value$")
	public void the_Notification_document_should_be_deleted_from_the_database_according_to_ttl_value()
			throws Throwable {
		long maxTime = System.currentTimeMillis() + 60000;
		List<String> notificationExit = null;
		String condition = "{\"subscriptionName\" : \"" + SUBSCRIPTION_NAME_3 + "\"}";
		LOGGER.debug("Checking deletion of notification document in db");
		while (System.currentTimeMillis() < maxTime) {
			notificationExit = mongoDBHandler.find(missedNotificationDatabase, missedNotificationCollection, condition);

			if (notificationExit.isEmpty()) {
				break;
			}
		}
		assertEquals(0, notificationExit.size());
	}

	@Then("^the Aggregated Object document should be deleted from the database according to ttl value$")
	public void the_Aggregated_Object_document_should_be_deleted_from_the_database_according_to_ttl_value()
			throws Throwable {
		LOGGER.debug("Checking delition of aggregated object in db");
		List<String> allObjects = null;
		allObjects = mongoDBHandler.getAllDocuments(dataBase, collection);
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
		mockServerClient.when(request().withMethod("POST").withPath(ENDPOINT))
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
}
