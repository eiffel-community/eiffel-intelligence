package com.ericsson.ei.subscriptions.query;

import static org.junit.Assert.assertEquals;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.spring.SpringFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; 

@Ignore
@AutoConfigureMockMvc
public class FreeStyleQueryTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeStyleQueryTestSteps.class);

    
    private static final String aggregatedObjJsonPath = "src/test/resources/AggregatedObject.json";
    private static final String missedNotificationJsonPath = "src/test/resources/MissedNotification.json";

    
    @Autowired
    private MockMvc mockMvc;
    private MvcResult mvcResult;
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private MongoProperties mongoProperties;
    
    @Autowired
    private MongoDBHandler mongoDBHandler;
    
    @Value("${spring.data.mongodb.database}")
    private String eiDatabseName;
    
    @Value("${aggregated.collection.name}")
    private String aggrCollectionName;
    
    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;
    
    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabaseName;

    private String aggrObj;
    private String missedNotificationObj;
    
    private ObjectMapper objMapper;
    
    public FreeStyleQueryTestSteps(){
        objMapper = new ObjectMapper();

        try {
            aggrObj = FileUtils.readFileToString(new File(aggregatedObjJsonPath), "UTF-8");
            missedNotificationObj = FileUtils.readFileToString(new File(missedNotificationJsonPath), "UTF-8");

        } catch (IOException e) {
            LOGGER.error("Failed to open test json files for test. Error message\n: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Given("^Aggregated object is created$")
    public void aggregated_object_is_created() throws Throwable {
        LOGGER.debug("Creating aggregated object in MongoDb");
        assertEquals(true, createDocumentInMongoDb(eiDatabseName, aggrCollectionName, aggrObj));
    }

    @Given("^Missed Notification object is created$")
    public void missed_notification_object_is_created() throws Throwable {
        LOGGER.debug("Missed Notification object has been created in MongoDb");
        assertEquals(true, createDocumentInMongoDb(missedNotificationDatabaseName, missedNotificationCollectionName, missedNotificationObj));
    }

    @Then("^Perform valid query on newly created Aggregated object")
    public void perform_valid_query_on_newly_created_aggregated_object() throws Throwable {
        String expectedTestCaseStartedEventId = "cb9d64b0-a6e9-4419-8b5d-a650c27c59ca";
        String entryPoint = "/queryAggregatedObject";
        String queryRequest = "{\"id\":\"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"}";
        String documentId = getDocumentIdBasedOnQuery(eiDatabseName, aggrCollectionName, queryRequest);
        LOGGER.debug("Got AggregateObject actual DocumentId after querying MongoDB: " + documentId);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("ID", documentId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        LOGGER.debug("Response of /queryAggregatedObject RestApi, Status Code: " + mvcResult.getResponse().getStatus() +
                           "\nResponse: " + mvcResult.getResponse().getContentAsString());
        
        JsonNode jsonNodeResult = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class);
        JsonNode responseEntityNode = jsonNodeResult.get("responseEntity");
        String responseEntityNodeFormatted = responseEntityNode.asText().replace("[", "").replace("]", "");
        JsonNode responseEntityFormattedJsonNode = new ObjectMapper().readValue(responseEntityNodeFormatted, JsonNode.class);
        
        LOGGER.debug("AggregatedObject from Response: " + responseEntityFormattedJsonNode.toString());
       
        String actualTestCaseStartedEventId = responseEntityFormattedJsonNode.get("testCaseExecutions").get("testCaseStartedEventId").asText();
        assertEquals(HttpStatus.OK.toString(), Integer.toString(mvcResult.getResponse().getStatus()));
        assertEquals("Failed to compare actual Aggregated Object:\n" + expectedTestCaseStartedEventId
                + "\nwith expected Aggregated Object:\n" + actualTestCaseStartedEventId,
                expectedTestCaseStartedEventId, actualTestCaseStartedEventId);
    }
    
    @And("^Perform an invalid query on same Aggregated object$")
    public void perform_invalid_query_on_newly_created_aggregated_object() throws Throwable {
        String invalidDocumentId = "5b3b4e1cccfbd55e6911dccc";
        String entryPoint = "/queryAggregatedObject";
        String expectedResponse = "{\"responseEntity\":\"[]\"}";
        
        LOGGER.debug("Trying a invalid query on /queryAggregatedObject RestApi with invalid documentId: " + invalidDocumentId);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("ID", invalidDocumentId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String responseAsString = mvcResult.getResponse().getContentAsString();
        int reponseStatusCode = mvcResult.getResponse().getStatus();
        LOGGER.debug("Response of /queryAggregatedObject RestApi, Status Code: " + reponseStatusCode +
                "\nResponse: " + responseAsString);
    
        assertEquals(HttpStatus.OK.toString(), Integer.toString(reponseStatusCode));
        assertEquals("Diffences between actual Aggregated Object:\n" + responseAsString
                + "\nand expected Aggregated Object:\n" + expectedResponse,
                expectedResponse, responseAsString);
    }
    
    @And("^Perform a query for missed notification$")
    public void perform_a_query_for_missed_notification() throws Throwable {
        
        String subscriptionName = "Subscription_1";
        String entryPoint = "/queryMissedNotifications";
        
        String expectedTestCaseStartedEventId = "cb9d64b0-a6e9-4419-8b5d-a650c27c59ca";

        
        LOGGER.debug("Check if MissedNotification and " + subscriptionName + " exist in Database");
        String queryRequest = "{\"subscriptionName\":\"" + subscriptionName + "\"}";
        String subscriptionNameCheck = objMapper.readValue(mongoDBHandler.find(missedNotificationDatabaseName, missedNotificationCollectionName, queryRequest).get(0), JsonNode.class).get("subscriptionName").asText();
        assertEquals("Expected subscriptionName in missed notification in Database is not as expected.",subscriptionName, subscriptionNameCheck);
        
        LOGGER.debug("Trying to query /queryMissedNotifications RestApi with subscriptionName: " + subscriptionName);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("SubscriptionName", subscriptionName)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseAsString = mvcResult.getResponse().getContentAsString();
        int reponseStatusCode = mvcResult.getResponse().getStatus();
        LOGGER.debug("Response of /queryMissedNotifications RestApi, Status Code: " + reponseStatusCode +
                "\nResponse: " + responseAsString);
        
        JsonNode jsonNodeResult = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class);
        JsonNode responseEntityNode = jsonNodeResult.get("responseEntity");
        String responseEntityNodeFormatted = responseEntityNode.asText().replace("[", "").replace("]", "");
        JsonNode responseEntityFormattedJsonNode = new ObjectMapper().readValue(responseEntityNodeFormatted, JsonNode.class);
        
        LOGGER.debug("AggregatedObject from Response: " + responseEntityFormattedJsonNode.toString());
       
        String actualTestCaseStartedEventId = responseEntityFormattedJsonNode.get("testCaseExecutions").get("testCaseStartedEventId").asText();
        assertEquals(HttpStatus.OK.toString(), Integer.toString(mvcResult.getResponse().getStatus()));
        assertEquals("Diffences between actual Missed Notification response:\n" + expectedTestCaseStartedEventId
                + "\nand expected  Missed Notification response:\n" + actualTestCaseStartedEventId,
                expectedTestCaseStartedEventId, actualTestCaseStartedEventId);
        
    }

    @And("^Check missed notification has been returned$")
    public void check_missed_notification_has_been_returned() throws Throwable {
        String expectedResponse = "{\"responseEntity\":\"[]\"}";
        String subscriptionName = "Subscription_1";
        String entryPoint = "/queryMissedNotifications";
        
        LOGGER.debug("Trying to query /queryMissedNotifications RestApi one more time with subscriptionName: " + subscriptionName);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("SubscriptionName", subscriptionName)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String responseAsString = mvcResult.getResponse().getContentAsString();
        int reponseStatusCode = mvcResult.getResponse().getStatus();
        LOGGER.debug("Response of /queryMissedNotifications RestApi, Status Code: " + reponseStatusCode +
                "\nResponse: " + responseAsString);
        
        assertEquals(HttpStatus.OK.toString(), Integer.toString(reponseStatusCode));
        assertEquals("Diffences between actual Missed Notification response:\n" + responseAsString
                + "\nand expected  Missed Notification response:\n" + expectedResponse,
                expectedResponse, responseAsString);
    }
    
    private boolean createDocumentInMongoDb(String databaseName, String collectionName, String objToBeInserted) {
        LOGGER.debug("Inserting Object to MongoDb.\nDatabase: " + databaseName
                     + "\nCollection: " + collectionName
                     + "\nDocument to be inserted\n: " + objToBeInserted);
        return mongoDBHandler.insertDocument(databaseName, collectionName, objToBeInserted);
    }
    
    private String getDocumentIdBasedOnQuery(String dataBaseName, String collectionName, String query) {
        String result = "";
        try {
            JsonNode jsonNodeId = objMapper.readValue(mongoDBHandler.find(dataBaseName, collectionName, query).get(0), JsonNode.class).get("_id");
            result = jsonNodeId.get("$oid").asText();
        } catch (IOException e) {
            LOGGER.error("Failed to read response of MongoDb document query: " + query);
            e.printStackTrace();
        }
        return result;
    }
}
