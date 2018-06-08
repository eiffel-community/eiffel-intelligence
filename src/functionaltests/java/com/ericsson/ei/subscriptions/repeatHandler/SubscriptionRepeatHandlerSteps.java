package com.ericsson.ei.subscriptions.repeatHandler;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.services.SubscriptionService;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestConfigs;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Ignore;
import org.skyscreamer.jsonassert.JSONAssert;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionRepeatHandlerSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRepeatHandlerSteps.class);
    private static final String RULES_FILE_PATH = "src/test/resources/TestExecutionObjectRules.json";
    private static final String AGGRAGERED_OBJECT_FILE_PATH = "/src/functionaltests/resources/aggragatedObject.json";
    private static final String SUBSCRIPTION_FILE_PATH = "src/test/resources/TestExecutionObjectRules.json";
    private static final String REPEAT_FLAG_SUBSCRIPTION_COLLECTIONS = "subscription_repeat_handler";
    private static final String SUBSCRIPTION_COLLECTIONS = "subscription";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private WaitListStorageHandler waitlist;

    @Autowired
    private MongodForTestsFactory testsFactory;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private int firstEventWaitTime = 0;
    private MvcResult result;
    private TestConfigs configs = new TestConfigs();

    @Given("^Subscription that will match this Aggregated Object$")
    public void subscription_that_will_match_this_Aggregated_Object() {
        LOGGER.debug("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @When("^Publish events on MessageBus$")
    public void send_events() throws Exception {
        //try {
//            String queueName = rmqHandler.getQueueName();
//            Channel channel = connectionFactory.newConnection().createChannel();
//            String exchangeName = "ei-poc-4";
//            configs.createExchange(exchangeName, queueName);
//            rulesHandler.setRulePath(getRulesFilePath());
//            List<String> eventNames = getEventNamesToSend();
//            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
//            int eventsCount = eventNames.size() ;
//
//            boolean alreadyExecuted = false;
//            for (String eventName : eventNames) {
//                JsonNode eventJson = parsedJSON.get(eventName);
//                String event = eventJson.toString();
//                channel.basicPublish(exchangeName, queueName, null, event.getBytes());
//                if (!alreadyExecuted) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(firstEventWaitTime);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        LOGGER.error(e.getMessage(), e);
//                    }
//                    alreadyExecuted = true;
//                }
//            }
//            // wait for all events to be processed
//            waitForEventsToBeProcessed(eventsCount);
//            checkResult(getCheckData());
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage(), e);
//        }
        LOGGER.debug("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @Then("^Subscription should only match Aggregated Object only one time$")
    public void subscription_should_only_match_Aggregated_Object_only_one_time() {

    }

    @Given("^Add subscription to MongoDB$")
    public void add_subscription_to_MongoDB(String subscriptionEndPoint) {

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name, String subscriptionEndPoint) {
        try {
            result = mockMvc.perform(MockMvcRequestBuilders.delete(subscriptionEndPoint + name).accept(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Then("^Check in MongoDB that subscription has been removed and check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_that_subscription_has_been_removed() {

    }

    @Then("^Check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_RepeatFlagHandler_collection_that_the_subscription_has_been_removed() {

    }

    @Then("^Subscription should match Aggregated Object at least two times$")
    public void subscription_should_only_match_AggregatedObject_at_least_two_times() {

    }

    private JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }
//
//    private String getRulesFilePath(){}
//
//    private String getEventsFilePath(){}
//
//    private List<String> getEventNamesToSend(){}
//
//    private Map<String, JsonNode> getCheckData() throws IOException{};

    private long countProcessedEvents(String database, String collection) {
        MongoDatabase db = configs.getMongoClient().getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    private void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void checkResult(final Map<String, JsonNode> checkData) {
        checkData.forEach((id, expectedJSON) -> {
            try {
                String document = objectHandler.findObjectById(id);
                JsonNode actualJSON = objectMapper.readTree(document);
                LOGGER.info("Complete aggregated object: " + actualJSON);
                JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
            } catch (IOException | JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }
}