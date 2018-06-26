package com.ericsson.ei.subscriptions.crud;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionCRUDSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCRUDSteps.class);

    private static final String SUBSCRIPTION_FILE_PATH = "src/functionaltests/resources/subscription_single.json";
    private static final String SUBSCRIPTION_UPDATED_FILE_PATH = "src/functionaltests/resources/subscription_single_updated.json";

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;
    private ObjectMapper mapper = new ObjectMapper();
    private static JSONArray jsonArray = null;

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) throws Throwable {
        result = mockMvc.perform(MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @When("^I make a POST request with valid \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_POST_request_with_valid_to_the_subscription_REST_API(String arg1, String endPoint) throws Throwable {
        String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_FILE_PATH), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        result = mockMvc.perform(MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON).content(jsonArray.toString())
            .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("^I get response code of (\\d+)$")
    public void i_get_response_code_of(int statusCode) throws Throwable {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }
    ///Scenario:1 ends ===============================================================================

    @When("^I make a GET request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_GET_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint) throws Throwable {
        result = mockMvc.perform(MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("^I get response code of (\\d+) and subscription name \"([^\"]*)\"$")
    public void i_get_response_code_of_and_subscription_name(int statusCode, String name) throws Throwable {
        Subscription[] subscription = mapper.readValue(result.getResponse().getContentAsString(), Subscription[].class);
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
        Assert.assertEquals(name, subscription[0].getSubscriptionName());
    }
    // Scenario:2 ends=========================================================================================

    @When("^I make a PUT request with modified notificationType as \"([^\"]*)\" to REST API \"([^\"]*)\"$")
    public void i_make_a_PUT_request_with_modified_notificationType_as_to_REST_API(String notificationType, String endPoint) throws Throwable {
        String readFileToString = FileUtils.readFileToString(new File(SUBSCRIPTION_UPDATED_FILE_PATH), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        result = mockMvc.perform(MockMvcRequestBuilders.put(endPoint).accept(MediaType.APPLICATION_JSON).content(jsonArray.toString())
            .contentType(MediaType.APPLICATION_JSON)).andReturn();
        LOGGER.info("Modified notificationType is:" + notificationType);
    }

    @Then("^I get response code of (\\d+) for successful updation$")
    public void i_get_response_code_of_for_successful_updation(int statusCode) throws Throwable {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @Then("^I can validate modified notificationType \"([^\"]*)\" with GET request at \"([^\"]*)\"$")
    public void i_can_validate_modified_notificationType_with_GET_request_at(String notificationType, String endPoint) throws Throwable {
        result = mockMvc.perform(MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON)).andReturn();
        JSONArray foundSubscriptions = new JSONObject(result.getResponse().getContentAsString()).getJSONArray("foundSubscriptions");
        Subscription subscription = mapper.readValue(foundSubscriptions.getJSONObject(0).toString(), Subscription.class);
        assertEquals(notificationType, subscription.getNotificationType());
    }
    //Scenario:3 ends ==========================================================================================

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint) throws Throwable {
        result = mockMvc.perform(MockMvcRequestBuilders.delete(endPoint + name).accept(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("^I get response code of (\\d+) for successful delete$")
    public void i_get_response_code_of_for_successful_delete(int statusCode) throws Throwable {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @Then("^My GET request with subscription name \"([^\"]*)\" at REST API \"([^\"]*)\" returns empty String \"([^\"]*)\"$")
    public void my_GET_request_with_subscription_name_at_REST_API_returns_empty_String(String name, String endPoint, String emptyString) throws Throwable {
        result = mockMvc.perform(MockMvcRequestBuilders.get(endPoint + name)
            .accept(MediaType.APPLICATION_JSON))
            .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertEquals(emptyString, response.getJSONArray("foundSubscriptions").toString());
        assertEquals(name, response.getJSONArray("notFoundSubscriptions").getString(0));
    }
    ////Scenario:4 ends ==========================================================================================
}
