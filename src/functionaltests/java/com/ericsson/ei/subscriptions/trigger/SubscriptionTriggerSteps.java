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
import com.rabbitmq.client.Channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;

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
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/EiffelEventsForTriggerTests.json";
    
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String collection;
    
    @Autowired
    private MockMvc mockMvc;
    MvcResult result;
    ObjectMapper mapper = new ObjectMapper();
    static JSONArray jsonArray = null;

    @Autowired
    private RmqHandler rmqHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionTriggerSteps.class);
    private MongoClient mongoClient;

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
            readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_JSON_PATH), "UTF-8");
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
            //checkResult(getCheckData());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
       
        LOGGER.debug("Eiffel events sent.");
    }

    @Then("^Subscriptions were triggered$")
    public void check_subscriptions_were_triggered() throws Throwable {
   	
    	//Mock SMTP
    	try (SimpleSmtpServer dumbster = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT)) {
            
    	    int port = dumbster.getPort();
    	    String sender = "christoffer@ericsson.se";
    	    String receiver = "anders@ericsson.se";
    	    String subject = "Hello!";
    	    String content = "This is a test for the mocked smtp server.";
            sendMessage(port, sender, subject, content, receiver);
            
            List<SmtpMessage> emails = dumbster.getReceivedEmails();
            assertEquals(emails.size(), 1);
            
            SmtpMessage email = emails.get(0);
            LOGGER.debug("Email: "+email.toString());
            assertEquals(email.getHeaderValue("From"), sender);
        }
    	
    	//Mock REST API
    	String baseURL ="127.0.0.1";
    	int port = SocketUtils.findAvailableTcpPort();
    	String path = "/stuff";
    	
    	
    	ClientAndServer mockServer = startClientAndServer(port);
    	
    	MockServerClient mockClient = new MockServerClient(baseURL, port);
    	mockClient
    	.when(
            request()
                .withMethod("GET")
                .withPath(path)
        )
        .respond(
            response()
                .withStatusCode(202)
                .withBody(
                    "Here is some stuff"
                )
        );
    	
    	HttpClient httpClient = HttpClients.createDefault();
    	final HttpGet httpGet = new HttpGet("http://"+baseURL+":"+port+path);
    	HttpResponse response = httpClient.execute(httpGet);
    	
    	String body = EntityUtils.toString(response.getEntity());
    	LOGGER.debug("HttpGet: "+body);
    	mockClient
    	.verify(
            request()
                .withPath(path),
            VerificationTimes.once()
        );
    	
    	mockClient.close();
    	mockServer.stop();
    }
    
    private void sendMessage(int port, String from, String subject, String body, String to) throws MessagingException {
        Properties mailProps = getMailProperties(port);
        Session session = Session.getInstance(mailProps, null);
        //session.setDebug(true);

        MimeMessage msg = createMessage(session, from, to, subject, body);
        Transport.send(msg);
    }
    
    private Properties getMailProperties(int port) {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", "" + port);
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }
    
    private MimeMessage createMessage(
        Session session, String from, String to, String subject, String body) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(body);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
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
