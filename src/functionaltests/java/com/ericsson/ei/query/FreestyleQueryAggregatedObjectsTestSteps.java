package com.ericsson.ei.query;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
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
public class FreestyleQueryAggregatedObjectsTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreestyleQueryAggregatedObjectsTestSteps.class);
    
    private static final String aggregatedObjJsonPath = "src/test/resources/AggregatedDocument.json";
    private static final String missedNotificationJsonPath = "src/test/resources/MissedNotification.json";

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
    
    private ObjectMapper objMapper;
    
    public FreestyleQueryAggregatedObjectsTestSteps() {
        objMapper = new ObjectMapper();

        try {
            aggrObj = FileUtils.readFileToString(new File(aggregatedObjJsonPath), "UTF-8");

        } catch (IOException e) {
            LOGGER.error("Failed to open test json files for test. Error message\n: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Given("^Aggregated object2 is created$")
    public void aggregated_object_is_created() throws Throwable {
        LOGGER.debug("Creating aggregated object in MongoDb");
        assertEquals(true, createDocumentInMongoDb(eiDatabaseName, aggrCollectionName, aggrObj));
    }

    @Then("^Perform valid freestyle query on newly created Aggregated object$")
    public void perform_valid_query_on_newly_created_aggregated_object() throws Throwable {
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
    public void perform_invalid_query_on_newly_created_aggregated_object() throws Throwable {
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
