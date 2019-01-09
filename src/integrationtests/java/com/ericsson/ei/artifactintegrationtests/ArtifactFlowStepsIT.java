package com.ericsson.ei.artifactintegrationtests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import util.IntegrationTestBase;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class })
public class ArtifactFlowStepsIT extends IntegrationTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactFlowStepsIT.class);

    private static final String UPSTREAM_INPUT_FILE = "src/test/resources/upStreamInput.json";
    private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules_new.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_events.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";
    private static final String AGGREGATED_OBJECT_ID = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String SUBSCRIPTIONS_PATH = "src/integrationtests/resources/subscriptionsForArtifactTest.json";
    private static final String host = "localhost";


    private long startTime;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.port}")
    int port;

    @Mock
    private ERQueryService erQueryService;

    @Autowired
    private RabbitTemplate rabbitMqTemplate;

    @Autowired
    MongoDBHandler mongoDBHandler;

    @Given("^subscriptions are uploaded$")
    public void subscriptions_are_created() throws URISyntaxException, IOException {
        startTime = System.currentTimeMillis();

        URL subscriptionsInput = new File(SUBSCRIPTIONS_PATH).toURI().toURL();
        ArrayNode subscriptionsJson = (ArrayNode) objectMapper.readTree(subscriptionsInput);

        HttpRequest getRequest = new HttpRequest(HttpMethod.POST);
        ResponseEntity response = getRequest.setHost(host)
                .setPort(port)
                .setEndpoint("/subscriptions")
                .addHeader("Content-type", "application/json")
                .setBody(subscriptionsJson.toString())
                .performRequest();
        assertEquals(200, response.getStatusCodeValue());
    }


    @When("^eiffel events are sent$")
    public void eiffel_events_are_sent() throws Throwable  {

        final URL upStreamInput = new File(UPSTREAM_INPUT_FILE).toURI().toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        if (upstreamJson != null) {
            for (JsonNode event : upstreamJson) {
                String eventStr = event.toString();
                rabbitMqTemplate.convertAndSend(eventStr);
            }
        }

        super.sendEventsAndConfirm();
    }

    @Then("^mongodb should contain mail\\.$")
    public void mongodb_should_contain_mails() throws Throwable {
        JsonNode newestMailJson = getMailFromDatabase();
        String createdDate = newestMailJson.get("created").get("$date").asText();

        long createdDateInMillis = ZonedDateTime.parse(createdDate).toInstant().toEpochMilli();
        assert(createdDateInMillis >= startTime): "Mail was not triggered. createdDateInMillis is less than startTime.";
    }

    @Override
    protected String getRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override
    protected String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Override
    protected int extraEventsCount() {
        return 8;
    }

    @Override
    protected List<String> getEventNamesToSend() {
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

    @Override
    protected Map<String, JsonNode> getCheckData() throws IOException {
        JsonNode expectedJSON = getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(AGGREGATED_OBJECT_ID, expectedJSON);
        return checkData;
    }

    private JsonNode getMailFromDatabase() throws IOException {
        ArrayList<String> allMails = mongoDBHandler.getAllDocuments(MAILHOG_DATABASE_NAME, "messages");
        String mailString = allMails.get(0);

        return objectMapper.readTree(mailString);
    }
}
