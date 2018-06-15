package com.ericsson.ei.subscriptions.trigger;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.JsonArray;
import gherkin.deps.com.google.gson.JsonElement;
import gherkin.deps.com.google.gson.JsonParser;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionTriggerSteps extends FunctionalTestBase {

    private static final String SUBSCRIPTION_WITH_JSON_PATH = "src/functionaltests/resources/SubscriptionForTriggerTests.json";
    private static final String SUBSCRIPTION_WITH_JSON_PATH_TMP = "src/functionaltests/resources/SubscriptionForTriggerTestsTmp.json";
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/EiffelEventsForTriggerTests.json";
    
    private static final String REST_ENDPOINT = "/rest_endpoint";
    private static final String REST_ENDPOINT_AUTH = "/rest_endpoint_auth";

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String collection;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    JavaMailSenderImpl mailSender;

    MvcResult result;
    ObjectMapper mapper = new ObjectMapper();
    static JSONArray jsonArray = null;
    SimpleSmtpServer smtpServer;
    ClientAndServer restServer;
    private MongoClient mongoClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionTriggerSteps.class);

    @Before
    public void beforeScenario() {
        LOGGER.debug("Starting SMTP and REST Mock Servers");
        try {
            int port = SocketUtils.findAvailableTcpPort();
            LOGGER.debug("Setting SMTP port to " + port);
            mailSender.setPort(port);
            smtpServer = SimpleSmtpServer.start(port);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        int port = SocketUtils.findAvailableTcpPort();
        LOGGER.debug("Setting REST port to " + port);
        restServer = startClientAndServer(port);
        
        LOGGER.debug("Creating temporary subscription file");
        prepareSubscriptionsNotification();
    }

    @After
    public void afterScenario() {       
        LOGGER.debug("Stopping SMTP and REST Mock Servers");
        smtpServer.stop();
        restServer.stop();
        
        LOGGER.debug("Deleting temporary subscription file");
        deleteFile(SUBSCRIPTION_WITH_JSON_PATH_TMP);
    }

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
            LOGGER.debug("Response code from mocked REST API: " + String.valueOf(result.getResponse().getStatus()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Given("^Subscriptions is setup using REST API \"([^\"]*)\"$")
    public void subscriptions_is_setup_using_REST_API(String endPoint) {
        String readFileToString = "";
        try {
            readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_JSON_PATH_TMP), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        ArrayList<String> subscriptions = new ArrayList<String>();
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(readFileToString);
        JsonArray array = rootNode.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            subscriptions.add(array.get(i).getAsJsonObject().get("subscriptionName").toString());
        }

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(readFileToString).contentType(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
            LOGGER.debug("Response code from REST when adding subscriptions: "
                    + String.valueOf(result.getResponse().getStatus()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        RequestBuilder getRequest = MockMvcRequestBuilders.get(endPoint);
        try {
            result = mockMvc.perform(getRequest).andReturn();
            LOGGER.debug("Response code from REST when getting subscriptions: "
                    + String.valueOf(result.getResponse().getStatus()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        LOGGER.debug("Checking that response contains all subscriptions");
        for (String sub : subscriptions) {
            try {
                assertTrue(result.getResponse().getContentAsString().contains(sub));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @When("^I send Eiffel events$")
    public void send_eiffel_events() throws Throwable {
        LOGGER.debug("About to sent Eiffel events.");
        try {
            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(EIFFEL_EVENTS_JSON_PATH);
            int eventsCount = 0;

            try {
                for (String eventName : eventNames) {
                    eventsCount++;
                    JsonNode eventJson = parsedJSON.get(eventName);
                    String event = eventJson.toString();
                    rmqHandler.publishObjectToWaitlistQueue(event);
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            // checkResult(getCheckData());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.debug("Eiffel events sent.");
    }

    @Then("^Subscriptions were triggered$")
    public void check_subscriptions_were_triggered() throws Throwable {
        // Mock SMTP
        String sender = System.getProperty("email.sender");
        List<SmtpMessage> emails = smtpServer.getReceivedEmails();
        assert (emails.size() > 0);

        for (SmtpMessage email : emails) {
            LOGGER.debug("Email: " + email.toString());
            //assertEquals(email.getHeaderValue("From"), sender);
        }

        // Mock REST API
        String baseURL = "localhost";
        int restPort = restServer.getPort();
        String restEndpoint = "/rest_endpoint";
        String restEndpointAuth = "/rest_endpoint_auth";
        MockServerClient mockClient = new MockServerClient(baseURL, restPort);

        // Set up endpoints
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT))
                .respond(response().withStatusCode(202).withBody("Success"));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_AUTH))
                .respond(response().withStatusCode(202).withBody("Success"));

        // Verify requests
        mockClient.verify(request().withPath(REST_ENDPOINT), VerificationTimes.atLeast(1));
        mockClient.verify(request().withPath(REST_ENDPOINT_AUTH), VerificationTimes.atLeast(1));

        mockClient.close();
    }

    private List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");
        eventNames.add("event_EiffelArtifactPublishedEvent_3_1");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3_1");
        eventNames.add("event_EiffelTestCaseStartedEvent_3_1");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3_1");
        return eventNames;
    }

    JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }
    
    public void copyFile(String filePath, String copyPath) {
        try {
            FileUtils.copyFile(new File(filePath), new File(copyPath));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void deleteFile(String filePath) {
        FileUtils.deleteQuietly(new File(filePath));
    }
    
    public void replaceText(String filePath, String regex, String replacement) {
        String content = "";
        try {
            content = FileUtils.readFileToString(new File(filePath), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if(!content.isEmpty()) {
            content = content.replaceAll(regex, replacement);
        }
        try {
            FileUtils.writeStringToFile(new File(filePath), content, "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void prepareSubscriptionsNotification() {
        copyFile(SUBSCRIPTION_WITH_JSON_PATH, SUBSCRIPTION_WITH_JSON_PATH_TMP);
        replaceText(SUBSCRIPTION_WITH_JSON_PATH_TMP, "\\$\\{rest.host\\}", "localhost");
        replaceText(SUBSCRIPTION_WITH_JSON_PATH_TMP, "\\$\\{rest.port\\}", String.valueOf(restServer.getPort()));
        replaceText(SUBSCRIPTION_WITH_JSON_PATH_TMP, "\\$\\{rest.endpoint\\}", REST_ENDPOINT);
        replaceText(SUBSCRIPTION_WITH_JSON_PATH_TMP, "\\$\\{rest.endpoint.auth\\}", REST_ENDPOINT_AUTH);
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    private void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, collection);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
