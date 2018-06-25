package com.ericsson.ei.subscriptions.trigger;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
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

    private static final String SUBSCRIPTION_WITH_JSON_PATH = "src/functionaltests/resources/subscription_multiple.json";
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";

    private static final String REST_ENDPOINT = "/rest";
    private static final String REST_ENDPOINT_AUTH = "/rest/with/auth";
    private static final String REST_ENDPOINT_PARAMS = "/rest/with/params";
    private static final String REST_ENDPOINT_AUTH_PARAMS = "/rest/with/auth/params";
    private static final String BASE_URL = "localhost";
    
    private static ObjectMapper objectMapper = new ObjectMapper();
    private List<String> subscriptionNames = new ArrayList<>();

    @Value("${email.sender}")
    private String sender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private JavaMailSenderImpl mailSender;

    private MvcResult result;
    private MvcResult postResult;
    private MvcResult getResult;
    private SimpleSmtpServer smtpServer;
    private ClientAndServer restServer;
    private MockServerClient mockClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionTriggerSteps.class);

    @Before("@SubscriptionTriggerScenario")
    public void beforeScenario() {
        setupSMTPServer();
        setupRestEndpoints();
    }

    @After("@SubscriptionTriggerScenario")
    public void afterScenario() {
        LOGGER.debug("Stopping SMTP and REST Mock Servers");
        try {
            smtpServer.stop();
            restServer.stop();
            mockClient.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
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

    @Given("^Subscriptions are setup using REST API \"([^\"]*)\"$")
    public void subscriptions_are_setup_using_REST_API(String endPoint) {
        String readFileToString = "";
        try {
            readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_JSON_PATH), "UTF-8");
            readFileToString = stringReplaceText(readFileToString);
            readSubscriptionNames(readFileToString);
            postSubscriptions(readFileToString, endPoint);
            validateSubscriptionsSuccessfullyAdded(endPoint);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } 
    }
    
    @Then("^Mail subscriptions were triggered$")
    public void check_mail_subscriptions_were_triggered() throws Throwable {
        LOGGER.debug("Verifying received emails.");
        List<SmtpMessage> emails = smtpServer.getReceivedEmails();
        assert(emails.size() > 0);
        
        for(SmtpMessage email : emails) {
            // assert correct sender.
            assertEquals(email.getHeaderValue("From"), sender);
            // assert given test case exist in body.
            assert(email.getBody().toString().contains("TC5"));
        }
    }
    
    @Then("^Rest subscriptions were triggered$")
    public void check_rest_subscriptions_were_triggered() throws Throwable {
        LOGGER.debug("Verifying REST requests.");
        assert(requestBodyContainsStatedValues(new JSONArray(mockClient.retrieveRecordedRequests(request().withPath(REST_ENDPOINT), Format.JSON))));
        assert(requestBodyContainsStatedValues(new JSONArray(mockClient.retrieveRecordedRequests(request().withPath(REST_ENDPOINT_AUTH), Format.JSON))));
        assert(requestBodyContainsStatedValues(new JSONArray(mockClient.retrieveRecordedRequests(request().withPath(REST_ENDPOINT_PARAMS), Format.JSON))));
        assert(requestBodyContainsStatedValues(new JSONArray(mockClient.retrieveRecordedRequests(request().withPath(REST_ENDPOINT_AUTH_PARAMS), Format.JSON))));
    }

    @When("^I send Eiffel events$")
    public void send_eiffel_events() {
        LOGGER.debug("About to send Eiffel events.");
        try {
            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(EIFFEL_EVENTS_JSON_PATH);
            int eventsCount = 0;

            for (String eventName : eventNames) {
                eventsCount++;
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();
                rmqHandler.publishObjectToWaitlistQueue(event);
            }

            assert(waitForEventsToBeProcessed(eventsCount));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.debug("Eiffel events sent.");
    }

    private void readSubscriptionNames(String readFileToString) {
        try {
            JSONArray array = new JSONArray(readFileToString);
            for (int i = 0; i < array.length(); i++) {
                subscriptionNames.add(array.getJSONObject(i).get("subscriptionName").toString());
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void postSubscriptions(String readFileToString, String endPoint) throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(readFileToString).contentType(MediaType.APPLICATION_JSON);

        postResult = mockMvc.perform(requestBuilder).andReturn();
        LOGGER.debug("Response code from REST when adding subscriptions: "
                + String.valueOf(postResult.getResponse().getStatus()));

        assertEquals(HttpStatus.OK.value(), postResult.getResponse().getStatus());
    } 

    private void validateSubscriptionsSuccessfullyAdded(String endPoint) throws Exception {
        RequestBuilder getRequest = MockMvcRequestBuilders.get(endPoint);
        getResult = mockMvc.perform(getRequest).andReturn();

        LOGGER.debug("Response code from REST when getting subscriptions: "
                + String.valueOf(getResult.getResponse().getStatus()));
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        LOGGER.debug("Checking that response contains all subscriptions");
        for (String subscriptionName : subscriptionNames) {
            assertTrue(getResult.getResponse().getContentAsString().contains(subscriptionName));
        }        
    }

    private boolean requestBodyContainsStatedValues(JSONArray jsonArray) throws JSONException {
        int tc5 = 0, successfull = 0;
        for(int i = 0; i < jsonArray.length(); i++){
            String requestBody = jsonArray.getString(i);
            if (requestBody.contains("TC5")) {
                tc5++;
            }
            if (requestBody.contains("SUCCESSFUL")) {
                successfull++;
            }
        }
        return (tc5 > 0 && successfull > 0);
    }

    private void setupRestEndpoints() {
        // Set up endpoints
        int port = SocketUtils.findAvailableTcpPort();
        restServer = startClientAndServer(port);
        
        LOGGER.debug("Setting up endponts on host '" + BASE_URL + "' and port '" + port + "'.");
        mockClient = new MockServerClient(BASE_URL, port);
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_AUTH))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_PARAMS))
                .respond(response().withStatusCode(201));
        mockClient.when(request().withMethod("POST").withPath(REST_ENDPOINT_AUTH_PARAMS))
                .respond(response().withStatusCode(201));        
    }
    
    private void setupSMTPServer() {
        try {
            int port = SocketUtils.findAvailableTcpPort();
            LOGGER.debug("Setting SMTP port to " + port);
            mailSender.setPort(port);
            smtpServer = SimpleSmtpServer.start(port);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");
        return eventNames;
    }

    private JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

    private String stringReplaceText(String text) {
        text = text.replaceAll("\\$\\{rest\\.host\\}", "localhost");
        text = text.replaceAll("\\$\\{rest\\.port\\}", String.valueOf(restServer.getPort()));
        text = text.replaceAll("\\$\\{rest\\.endpoint\\}", REST_ENDPOINT);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\}", REST_ENDPOINT_AUTH);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.params\\}", REST_ENDPOINT_PARAMS);
        text = text.replaceAll("\\$\\{rest\\.endpoint\\.auth\\.params\\}", REST_ENDPOINT_AUTH_PARAMS);
        return text;
    }
}
