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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.subscriptions.trigger.SubscriptionTriggerSteps;
import com.ericsson.ei.utils.FunctionalTestBase;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class FreeStyleQueryTestSteps extends FunctionalTestBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeStyleQueryTestSteps.class);

    
    private static final String aggregatedObjJsonPath = "src/test/resources/AggregatedObject.json";
    private static final String missedNotificationJsonPath = "src/test/resources/MissedNotification.json";

    @Autowired
    private MockMvc mockMvc;
    MvcResult result;
    

    @Given("^Aggregated object is created$")
    public void aggregated_object_is_created() throws Throwable {
        LOGGER.debug("Creating aggregated object in MongoDb");
    }

    @Given("^Missed Notification object is created$")
    public void missed_notification_object_is_created() throws Throwable {
        LOGGER.debug("Missed Notification object has been created in MongoDb");
    }

    @Then("^Perform valid query on newly created Aggregated object")
    public void check_subscriptions_were_triggered() throws Throwable {

    }
    
    @When("^I send events$")
    public void send_events() throws Throwable {
        LOGGER.debug("Sending events");
    }
}
