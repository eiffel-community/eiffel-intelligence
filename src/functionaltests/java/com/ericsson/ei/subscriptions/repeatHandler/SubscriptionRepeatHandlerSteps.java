package com.ericsson.ei.subscriptions.repeatHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.ericsson.ei.subscription.RunSubscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "rules.path: src/test/resources/TestExecutionObjectRules.json",
        "spring.data.mongodb.database: SubscriptionRepeatHandlerSteps",
        "failed.notifications.collection.name: SubscriptionRepeatHandlerSteps-failedNotifications",
        "rabbitmq.exchange.name: SubscriptionRepeatHandlerSteps-exchange",
        "rabbitmq.consumer.name: SubscriptionRepeatHandlerStepsConsumer" })
public class SubscriptionRepeatHandlerSteps extends FunctionalTestBase {

    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/functionaltests/resources/aggregatedObject.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/TestExecutionTestEvents.json";
    private static final String REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS_WITH_ONE_MATCH = "src/functionaltests/resources/subscriptionRepeatHandlerOneMatch.json";
    private static final String REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS_WITH_TWO_MATCH = "src/functionaltests/resources/subscriptionRepeatHandlerTwoMatch.json";
    private static final String AGGREGATED_OBJECT_ID = "b46ef12d-25gb-4d7y-b9fd-8763re66de47";

    private JSONObject subscriptionWithOneMatch;
    private JSONObject subscriptionWithTwoMatch;
    private String subscriptionStrWithOneMatch;
    private String subscriptionStrWithTwoMatch;
    private String aggregatedObject;
    private MongoCondition subscriptionIdMatchedAggrIdObjQuery;
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${aggregations.collection.name}")
    private String collectionName;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${subscriptions.repeat.handler.collection.name}")
    private String repeatFlagHandlerCollection;

    @LocalServerPort
    private int applicationPort;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private RunSubscription runSubscription;

    @Before("@SubscriptionRepeatTrue or @SubscriptionRepeatFalse")
    public void beforeScenario() throws IOException, JSONException {
        mongoDBHandler.insertDocument(dataBaseName, collectionName,
                eventManager.getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH).toString());
        subscriptionStrWithOneMatch = FileUtils.readFileToString(
                new File(REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS_WITH_ONE_MATCH), "UTF-8");
        subscriptionStrWithTwoMatch = FileUtils.readFileToString(
                new File(REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS_WITH_TWO_MATCH), "UTF-8");
        aggregatedObject = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "UTF-8");
        subscriptionWithOneMatch = new JSONObject(subscriptionStrWithOneMatch);
        subscriptionWithTwoMatch = new JSONObject(subscriptionStrWithTwoMatch);
    }

    @Given("^Publish events on Message Bus$")
    public void publish_events_on_Message_Bus() throws IOException, InterruptedException {
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EVENTS_FILE_PATH, eventNamesToSend);
        List<String> arguments = new ArrayList<>();
        arguments.add("ongoing=true");
        List<String> missingArguments = dbManager.verifyAggregatedObjectInDB(arguments);
        assertEquals("The following arguments are missing in the Aggregated Object in mongoDB: "
                + missingArguments.toString(), 0, missingArguments.size());
    }

    @When("^In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId$")
    public void in_MongoDb_RepeatFlagHandler_and_subscription_collections_the_subscription_has_matched_the_AggrObjectId()
            throws IOException {
        processSubscription(subscriptionStrWithOneMatch, subscriptionWithOneMatch);
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection,
                subscriptionIdMatchedAggrIdObjQuery);
        assertEquals(1, resultRepeatFlagHandler.size());
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", getAggregatedObjectId(resultRepeatFlagHandler, 0));
    }

    @Then("^I make a DELETE request with subscription name \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name,
            String subscriptionEndPoint) throws Exception {
        HttpRequest deleteRequest = new HttpRequest(HttpRequest.HttpMethod.DELETE);
        ResponseEntity response = deleteRequest.setHost(getHostName())
                                               .setPort(applicationPort)
                                               .addHeader("content-type", "application/json")
                                               .addHeader("Accept", "application/json")
                                               .setEndpoint(subscriptionEndPoint + name)
                                               .performRequest();
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
    }

    @Then("^Check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_RepeatFlagHandler_collections_that_the_subscription_has_been_removed()
            throws IOException, InterruptedException {
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection,
                subscriptionIdMatchedAggrIdObjQuery);
        assertEquals("[]", resultRepeatFlagHandler.toString());
        final MongoCondition condition = MongoCondition.idCondition(AGGREGATED_OBJECT_ID);
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, condition));
    }

    @When("^In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId at least two times$")
    public void in_MongoDb_RepeatFlagHandler_collection_the_subscription_has_matched_the_AggrObjectId_at_least_two_times()
            throws IOException {
        processSubscription(subscriptionStrWithTwoMatch, subscriptionWithTwoMatch);
        List<String> resultRepeatFlagHandler = mongoDBHandler.find(dataBaseName,
                repeatFlagHandlerCollection,
                subscriptionIdMatchedAggrIdObjQuery);
        assertEquals(1, resultRepeatFlagHandler.size());
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", getAggregatedObjectId(resultRepeatFlagHandler, 0));
        assertEquals("\"" + AGGREGATED_OBJECT_ID + "\"", getAggregatedObjectId(resultRepeatFlagHandler, 1));
    }

    public List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelTestCaseFinishedEvent_2");
        eventNames.add("event_EiffelActivityFinishedEvent");
        return eventNames;
    }

    /**
     * Process list of documents which gotten from RepeatFlagHandler collection
     *
     * @param resultRepeatFlagHandler list from RepeatFlagHandler collection
     * @param index
     * @return value of aggregatedObjectId
     */
    private String getAggregatedObjectId(List<String> resultRepeatFlagHandler, int index) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(resultRepeatFlagHandler.get(0)).getAsJsonObject();
        JsonObject requirements = jsonObject.get("requirements").getAsJsonObject();
        return requirements.get(String.valueOf(index)).getAsJsonArray().get(0).toString();
    }

    /**
     * Adding subscription to RepeatFlagHandler collection
     *
     * @param subscriptionStrValue
     * @param subscriptionObject
     * @throws IOException
     */
    private void processSubscription(String subscriptionStrValue, JSONObject subscriptionObject) throws IOException {
        Subscription subscription = mapper.readValue(subscriptionObject.toString(), Subscription.class);
        subscriptionService.addSubscription(subscription);
        String expectedSubscriptionName = subscription.getSubscriptionName();
        JsonNode subscriptionJson = mapper.readTree(subscriptionStrValue);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        assertTrue(runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator, subscriptionJson,
                AGGREGATED_OBJECT_ID));
        subscriptionIdMatchedAggrIdObjQuery = MongoCondition.subscriptionCondition(
                expectedSubscriptionName);
    }
}