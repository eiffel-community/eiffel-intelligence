package com.ericsson.ei.subscriptions.query;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.utils.test.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.subscriptions.trigger.SubscriptionTriggerSteps;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestConfigs;
import com.mongodb.MongoClient;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
    
    public FreeStyleQueryTestSteps(){
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
        String entryPoint = "/query";
        String queryRequest = "{\"criteria\" :{\"_id\":\"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"}}";
        System.out.println("KKKKAAAAALLLLLLEEEEEEEEEEEEEEEEEEEEEE");
        mvcResult = mockMvc.perform(get(entryPoint)
                .param("request", queryRequest)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        System.out.println("RESULT: " + mvcResult.getResponse().getContentAsString());
//        assertEquals(HttpStatus.OK, mvcResult.getResponse().getStatus());
    }
    
//    @And("^Perform invalid query on newly created Aggregated object")
//    public void perform_invalid_query_on_newly_created_aggregated_object() throws Throwable {
//
//    }
//    
//    @And("^Perform invalid query on newly created Aggregated object one more time")
//    public void perform_invalid_query_on_newly_created_aggregated_object_one_more_time() throws Throwable {
//
//    }
//    
//    @And("Check missed notification has been returned")
//    public void check_missed_notification_has_been_returned() {
//        
//    }
    
    
    private boolean createDocumentInMongoDb(String databaseName, String collectionName, String objToBeInserted) {
        LOGGER.debug("Inserting Object to MongoDb.\nDatabase: " + databaseName
                     + "\nCollection: " + collectionName
                     + "\nDocument to be inserted\n: " + objToBeInserted);
        return mongoDBHandler.insertDocument(databaseName, collectionName, objToBeInserted);
    }
}
