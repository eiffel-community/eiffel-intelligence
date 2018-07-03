package com.ericsson.ei.subscriptions.repeatHandler;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.ericsson.ei.subscriptionhandler.RunSubscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionRepeatHandlerSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRepeatHandlerSteps.class);

    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/functionaltests/resources/aggragatedObject.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/TestExecutionTestEvents.json";
    private static final String RULES_FILE_PATH = "src/test/resources/TestExecutionObjectRules.json";
    private static final String REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS = "src/functionaltests/resources/subscriptionRepeatHandler.json";
    private static final String AGGREGATED_OBJECT_ID = "b46ef12d-25gb-4d7y-b9fd-8763re66de47";

    private JSONObject jsonArray;
    private String subscriptionStr;
    private String aggregatedObject;
    private String subscriptionIdMatchedAggrIdObjQuery;
    private MvcResult result;
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${aggregated.collection.name}")
    private String collectionName;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${subscription.collection.repeatFlagHandlerName}")
    private String repeatFlagHandlerCollection;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private RunSubscription runSubscription;

    @Before
    public void init() throws IOException, JSONException {
        assertTrue(mongoDBHandler.insertDocument(dataBaseName, collectionName, getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH).toString()));
        subscriptionStr = FileUtils.readFileToString(new File(REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS), "UTF-8");
        aggregatedObject = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "UTF-8");
        jsonArray = new JSONObject(subscriptionStr);
    }

    @Given("^Publish events on Message Bus$")
    public void publish_events_on_Message_Bus() throws IOException, InterruptedException {
        rulesHandler.setRulePath(RULES_FILE_PATH);
        sendEiffelEvents(EVENTS_FILE_PATH);
        List<String> arguments = new ArrayList<>();
        arguments.add("ongoing=true");
        List<String> missingArguments = verifyAggregatedObjectInDB(arguments);
        assertEquals("The following arguments are missing in the Aggregated Object in mongoDB: "
                + missingArguments.toString(), 0, missingArguments.size());
    }

    @When("^In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId$")
    public void in_MongoDb_RepeatFlagHandler_and_subscription_collections_the_subscription_has_matched_the_AggrObjectId() throws IOException {
        Subscription subscription = mapper.readValue(jsonArray.toString(), Subscription.class);
        assertTrue(subscriptionService.addSubscription(subscription));
        String expectedSubscriptionName = subscription.getSubscriptionName();
        JsonNode subscriptionJson = mapper.readTree(subscriptionStr);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        assertTrue(runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator, subscriptionJson, AGGREGATED_OBJECT_ID));
        subscriptionIdMatchedAggrIdObjQuery = "{ \"subscriptionId\" : \"" + expectedSubscriptionName + "\"}";
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection, subscriptionIdMatchedAggrIdObjQuery);
        String aggregatedObjectId = new JsonParser().parse(resultRepeatFlagHandler.get(0)).getAsJsonObject().get("requirements").getAsJsonObject().get("0").getAsJsonArray().get(0).toString();
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", aggregatedObjectId);
    }

    @Then("^I make a DELETE request with subscription name \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name, String subscriptionEndPoint) {
        try {
            result = mockMvc.perform(MockMvcRequestBuilders.delete(subscriptionEndPoint + name).accept(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Then("^Check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_RepeatFlagHandler_collections_that_the_subscription_has_been_removed() throws IOException {
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection, subscriptionIdMatchedAggrIdObjQuery);
        assertEquals("[]", resultRepeatFlagHandler.toString());
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH).toString()));
    }

    @When("^In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId at least two times$")
    public void in_MongoDb_RepeatFlagHandler_collection_the_subscription_has_matched_the_AggrObjectId_at_least_two_times() throws IOException {
        Subscription subscription = mapper.readValue(jsonArray.toString(), Subscription.class);
        assertTrue(subscriptionService.addSubscription(subscription));
        String expectedSubscriptionName = subscription.getSubscriptionName();
        JsonNode subscriptionJson = mapper.readTree(subscriptionStr);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        assertTrue(runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator, subscriptionJson, AGGREGATED_OBJECT_ID));
        subscriptionIdMatchedAggrIdObjQuery = "{ \"subscriptionId\" : \"" + expectedSubscriptionName + "\"}";
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection, subscriptionIdMatchedAggrIdObjQuery);
        String aggregatedObjectId = new JsonParser().parse(resultRepeatFlagHandler.get(0)).getAsJsonObject().get("requirements").getAsJsonObject().get("0").getAsJsonArray().get(0).toString();
        String aggregatedObjectIdTwo = new JsonParser().parse(resultRepeatFlagHandler.get(0)).getAsJsonObject().get("requirements").getAsJsonObject().get("0").getAsJsonArray().get(0).toString();
        assertEquals(2, resultRepeatFlagHandler.size());
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", aggregatedObjectId);
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", aggregatedObjectIdTwo);
    }

    @Override
    public List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelTestCaseFinishedEvent_2");
        eventNames.add("event_EiffelActivityFinishedEvent");
        return eventNames;
    }
}