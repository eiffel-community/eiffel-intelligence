package com.ericsson.ei.subscriptions.crud;

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

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionCRUDSteps extends FunctionalTestBase {
    
    private static final String subscriptionJsonPath = "src/functionaltests/resources/subscription_single.json";
    private static final String subscriptionJsonPathUpdated = "src/functionaltests/resources/subscription_single_updated.json";

    @Autowired
    private MockMvc mockMvc;
    MvcResult result;
    ObjectMapper mapper = new ObjectMapper();
    static JSONArray jsonArray = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCRUDSteps.class);
    

    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @When("^I make a POST request with valid \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_POST_request_with_valid_to_the_subscription_REST_API(String arg1, String endPoint) {
        String readFileToString = "";
        try {
            readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            jsonArray = new JSONArray(readFileToString);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(jsonArray.toString()).contentType(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Then("^I get response code of (\\d+)$")
    public void i_get_response_code_of(int statusCode) {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }
    /// ===============================================================================

    @When("^I make a GET request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_GET_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint) {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Then("^I get response code of (\\d+) and subscription name \"([^\"]*)\"$")
    public void i_get_response_code_of_and_subscription_name(int statusCode, String name) {
        Subscription[] subscription = null;
        try {
            subscription = mapper.readValue(result.getResponse().getContentAsString(), Subscription[].class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
        Assert.assertEquals(name, subscription[0].getSubscriptionName());
    }

    // =========================================================================================

    @When("^I make a PUT request with modified notificationType as \"([^\"]*)\" to REST API \"([^\"]*)\"$")
    public void i_make_a_PUT_request_with_modified_notificationType_as_to_REST_API(String notificationType,
            String endPoint){
        String readFileToString = null;
        try {
            readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPathUpdated), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            jsonArray = new JSONArray(readFileToString);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        RequestBuilder requestBuilder = MockMvcRequestBuilders.put(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(jsonArray.toString()).contentType(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Modified notificationType is:" + notificationType);
    }

    @Then("^I get response code of (\\d+) for successful updation$")
    public void i_get_response_code_of_for_successful_updation(int statusCode) {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @Then("^I can validate modified notificationType \"([^\"]*)\" with GET request at \"([^\"]*)\"$")
    public void i_can_validate_modified_notificationType_with_GET_request_at(String notificationType, String endPoint) {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Subscription[] subscription = null;
        try {
            subscription = mapper.readValue(result.getResponse().getContentAsString(), Subscription[].class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(notificationType, subscription[0].getNotificationType());
    }

    // ==========================================================================================

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name,
            String endPoint) {
        endPoint = endPoint + name;
        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(endPoint).accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Then("^I get response code of (\\d+) for successful delete$")
    public void i_get_response_code_of_for_successful_delete(int statusCode) {
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
        SubscriptionResponse subscriptionResponse = null;
        try {
            subscriptionResponse = mapper.readValue(result.getResponse().getContentAsString().toString(),
                    SubscriptionResponse.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }

    @Then("^My GET request with subscription name \"([^\"]*)\" at REST API \"([^\"]*)\" returns empty String \"([^\"]*)\"$")
    public void my_GET_request_with_subscription_name_at_REST_API_returns_empty_String(String name, String endPoint, String emptyString) {
        endPoint = endPoint + name;
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint)
                .accept(MediaType.APPLICATION_JSON);
        try {
            result = mockMvc.perform(requestBuilder).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            assertEquals(emptyString, result.getResponse().getContentAsString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
