package com.ericsson.ei.query;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.utils.FunctionalTestBase;


@Ignore
@AutoConfigureMockMvc
public class QueryAggregatedObjectsTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAggregatedObjectsTestSteps.class);

    
    private static final String AGGREGATED_OBJ_JSON_PATH = "src/test/resources/AggregatedDocument.json";
    private static final String MISSED_NOTIFICATION_JSON_PATH = "src/test/resources/MissedNotification.json";

    
    @Autowired
    private MockMvc mockMvc;
    private MvcResult mvcResult;
    
    @Autowired
    private MongoDBHandler mongoDBHandler;
    
    @Value("${spring.data.mongodb.database}")
    private String eiDatabaseName;
    
    @Value("${aggregated.collection.name}")
    private String aggrCollectionName;
    
    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;
    
    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabaseName;

    private String aggrObj;
    private String missedNotificationObj;
    
    private ObjectMapper objMapper;
    
    public QueryAggregatedObjectsTestSteps() {
        objMapper = new ObjectMapper();

        try {
            aggrObj = FileUtils.readFileToString(new File(AGGREGATED_OBJ_JSON_PATH), "UTF-8");
            missedNotificationObj = FileUtils.readFileToString(new File(MISSED_NOTIFICATION_JSON_PATH), "UTF-8");

        } catch (IOException e) {
            LOGGER.error("Failed to open test json files for test. Error message\n: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Given("^Aggregated object is created$")
    public void aggregated_object_is_created() throws Throwable {
        LOGGER.debug("Creating aggregated object in MongoDb");
        assertEquals(true, createDocumentInMongoDb(eiDatabaseName, aggrCollectionName, aggrObj));
    }

    @Given("^Missed Notification object is created$")
    public void missed_notification_object_is_created() throws Throwable {
        LOGGER.debug("Missed Notification object has been created in MongoDb");
        assertEquals(true, createDocumentInMongoDb(missedNotificationDatabaseName, missedNotificationCollectionName, missedNotificationObj));
    }

    @Then("^Perform valid query on created Aggregated object")
    public void perform_valid_query_on_newly_created_aggregated_object() throws Throwable {
        final String expectedTestCaseFinishedEventId = "cb9d64b0-a6e9-4419-8b5d-a650c27c1111";
        final String entryPoint = "/queryAggregatedObject";
        final String documentId = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
        LOGGER.debug("Got AggregateObject actual DocumentId after querying MongoDB: " + documentId);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("ID", documentId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        LOGGER.debug("Response of /queryAggregatedObject RestApi, Status Code: " + mvcResult.getResponse().getStatus() +
                           "\nResponse: " + mvcResult.getResponse().getContentAsString());
        
        JsonNode jsonNodeResult = objMapper.readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class);
        JsonNode responseEntityNode = objMapper.readValue(jsonNodeResult.get("responseEntity").asText(), JsonNode.class); 
        String responseEntityFormattedString = responseEntityNode.toString().
                substring(1, responseEntityNode.toString().length()-1);
        
        LOGGER.debug("AggregatedObject from Response: " + responseEntityFormattedString);
        JsonNode responseEntityFormattedJsonNode = objMapper.readValue(responseEntityFormattedString, JsonNode.class);
        String actualTestCaseFinishedEventId = responseEntityFormattedJsonNode.get("aggregatedObject")
                .get("testCaseExecutions").get(0).get("testCaseFinishedEventId").asText();
        
        assertEquals(HttpStatus.OK.toString(), Integer.toString(mvcResult.getResponse().getStatus()));
        assertEquals("Failed to compare actual Aggregated Object TestCaseFinishedEventId:\n" + actualTestCaseFinishedEventId
                + "\nwith expected Aggregated Object TestCaseFinishedEventId:\n" + expectedTestCaseFinishedEventId,
                expectedTestCaseFinishedEventId, actualTestCaseFinishedEventId);
    }
    
    @And("^Perform an invalid query on same Aggregated object$")
    public void perform_invalid_query_on_created_aggregated_object() throws Throwable {
        final String invalidDocumentId = "6acc3c87-75e0-4aaa-88f5-b1a5d4e6cccc";
        final String entryPoint = "/queryAggregatedObject";
        final String expectedResponse = "{\"responseEntity\":\"[]\"}";
        
        LOGGER.debug("Trying an invalid query on /queryAggregatedObject RestApi with invalid documentId: " + invalidDocumentId);
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
    
    @And("^Perform valid freestyle query on created Aggregated object$")
    public void perform_valid_freestyle_query_on_created_aggregated_object() throws Throwable {
        final String expectedAggrId = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
        final String entryPoint = "/query";
        final String queryAggrObj = "{\"criteria\" :{\"aggregatedObject.id\" : \"" + expectedAggrId + "\", \"aggregatedObject.gav.groupId\" : \"com.mycompany.myproduct\"}}";

        LOGGER.debug("Freestyle querying for the AggregatedObject with criteria: " + queryAggrObj);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("request", queryAggrObj)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        LOGGER.debug("Response of /query RestApi, Status Code: " + mvcResult.getResponse().getStatus() +
                           "\nResponse: " + mvcResult.getResponse().getContentAsString());
        
        JsonNode jsonNodeResult = objMapper.readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class);
        JsonNode aggrObjResponse = objMapper.readValue(jsonNodeResult.get(0).get("aggregatedObject").toString(), JsonNode.class); 

        String actualAggrObjId = aggrObjResponse.get("id").asText();
        LOGGER.debug("AggregatedObject id from Response: " + actualAggrObjId);
   
        
        assertEquals(HttpStatus.OK.toString(), Integer.toString(mvcResult.getResponse().getStatus()));
        assertEquals("Failed to compare actual Aggregated Object Id:\n" + actualAggrObjId
                + "\nwith expected Aggregated Object Id:\n" + expectedAggrId,
                expectedAggrId, actualAggrObjId);
    }
    
    @And("^Perform an invalid freesyle query on Aggregated object$")
    public void perform_invalid_freestyle_query_on_created_aggregated_object() throws Throwable {
        final String invalidAggrId = "6acc3c87-75e0-4b6d-88f5-b1aee4e62b43";
        final String entryPoint = "/query";
        final String queryAggrObj = "{\"criteria\" :{\"aggregatedObject.id\" : \"" + invalidAggrId + "\" }}";
        final String expectedResponse = "[]";
        
        LOGGER.debug("Trying an invalid query on /query RestApi with invalid criteria query: " + queryAggrObj);
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("request", queryAggrObj)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String responseAsString = mvcResult.getResponse().getContentAsString();
        int reponseStatusCode = mvcResult.getResponse().getStatus();
        LOGGER.debug("Response of /query RestApi, Status Code: " + reponseStatusCode +
                "\nResponse: " + responseAsString);
    
        assertEquals(HttpStatus.OK.toString(), Integer.toString(reponseStatusCode));
        assertEquals("Diffences between actual Aggregated Object:\n" + responseAsString
                + "\nand expected Aggregated Object:\n" + expectedResponse,
                expectedResponse, responseAsString);
    }
    
    @And("^Perform a query for missed notification$")
    public void perform_a_query_for_missed_notification() throws Throwable {
        
        final String subscriptionName = "Subscription_1";
        final String entryPoint = "/queryMissedNotifications";
        final String expectedTestCaseStartedEventId = "cb9d64b0-a6e9-4419-8b5d-a650c27c59ca";
 
        LOGGER.debug("Check if MissedNotification and " + subscriptionName + " exist in Database");
        final String queryRequest = "{\"subscriptionName\":\"" + subscriptionName + "\"}";
        String subscriptionNameCheck = objMapper.readValue(mongoDBHandler
                .find(missedNotificationDatabaseName, missedNotificationCollectionName, queryRequest).get(0),
                JsonNode.class).get("subscriptionName").asText();
        assertEquals("Expected subscriptionName in missed notification in Database is not as expected.",
                subscriptionName, subscriptionNameCheck);
        
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
        
        JsonNode jsonNodeResult = objMapper.readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class);
        JsonNode responseEntityNode = jsonNodeResult.get("responseEntity");
        String responseEntityNodeFormatted = responseEntityNode.asText().replace("[", "").replace("]", "");
        JsonNode responseEntityFormattedJsonNode = objMapper.readValue(responseEntityNodeFormatted, JsonNode.class);
        
        LOGGER.debug("AggregatedObject from Response: " + responseEntityFormattedJsonNode.toString());
       
        String actualTestCaseStartedEventId = responseEntityFormattedJsonNode
                .get("testCaseExecutions").get("testCaseStartedEventId").asText();
        assertEquals(HttpStatus.OK.toString(), Integer.toString(mvcResult.getResponse().getStatus()));
        assertEquals("Diffences between actual Missed Notification response TestCaseStartedEventId:\n" + actualTestCaseStartedEventId
                + "\nand expected  Missed Notification response TestCaseStartedEventId:\n" + expectedTestCaseStartedEventId,
                expectedTestCaseStartedEventId, actualTestCaseStartedEventId);
        
    }

    @And("^Check missed notification has been returned$")
    public void check_missed_notification_has_been_returned() throws Throwable {
        final String expectedResponse = "{\"responseEntity\":\"[]\"}";
        final String subscriptionName = "Subscription_1";
        final String entryPoint = "/queryMissedNotifications";
        
        LOGGER.debug("Trying to query /queryMissedNotifications RestApi one more time with subscriptionName: "
                    + subscriptionName);
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
    
    /**
     * Method that creates a document in MongoDb database.
     * 
     * @param databaseName - Name of the database in MongoDb to use.
     * @param collectionName - Name of the collection in MongoDb to use.
     * @param objToBeInserted - Object in string format to be inserted to database.
     * 
     * @return boolean - Returns true or false depending if object/document was successfully created in database.
     * 
     */
    private boolean createDocumentInMongoDb(String databaseName, String collectionName, String objToBeInserted) {
        LOGGER.debug("Inserting Object to MongoDb.\nDatabase: " + databaseName
                     + "\nCollection: " + collectionName
                     + "\nDocument to be inserted\n: " + objToBeInserted);
        return mongoDBHandler.insertDocument(databaseName, collectionName, objToBeInserted);
    }

}
