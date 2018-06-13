package com.ericsson.ei.subscriptions.trigger;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
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

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionTriggerSteps extends FunctionalTestBase {

    private static final String SUBSCRIPTION_WITH_JSON_PATH = "src/functionaltests/resources/SubscriptionForTriggerTests.json";
    
	@Autowired
	private MockMvc mockMvc;
	MvcResult result;
	ObjectMapper mapper = new ObjectMapper();
	static JSONArray jsonArray = null;
	
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionTriggerSteps.class);

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
            LOGGER.debug("Response code from mocked REST API: " + String.valueOf(result.getResponse().getStatus()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());        		
    }    	

    @Given("^Subscriptions is setup using REST API \"([^\"]*)\"$")
    public void subscriptions_is_setup_using_REST_API(String endPoint) {
        String readFileToString = "";
        try {
            readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_WITH_JSON_PATH), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(readFileToString).contentType(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
            LOGGER.debug("Response code from REST when adding subscriptions: " + String.valueOf(result.getResponse().getStatus()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());  
    }
    
    @When("^I send Eiffel events$")
    public void send_eiffel_events() throws Throwable {
        LOGGER.debug("Sending events");
    }

    @Then("^Subscriptions were triggered$")
    public void check_subscriptions_were_triggered() throws Throwable {
    	LOGGER.debug("I have seven chickens that eat tigers.");
    }

}
