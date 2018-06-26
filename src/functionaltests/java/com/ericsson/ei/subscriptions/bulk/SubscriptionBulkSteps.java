package com.ericsson.ei.subscriptions.bulk;

import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionBulkSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionBulkSteps.class);

    private static final String TEST_RESOURCES_PATH = "src/functionaltests/resources";

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private JSONArray subscriptions;

    private JSONArray retrievedSubscriptions;

    @Given("^file with subscriptions \"([^\"]*)\"$")
    public void file_with_subscriptions(String subscriptionsFileName) throws Throwable {
        String fileContent = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + subscriptionsFileName), "UTF-8");
        subscriptions = new JSONArray(fileContent);
    }

    @When("^make a POST request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_POST_request_with_list_of_subscriptions_to_the_subscription_REST_API(String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(subscriptions.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a GET request with list of subscriptions names \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void make_a_GET_request_with_list_of_subscriptions_names_to_the_subscription_REST_API(String subscriptionsNamesList, String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + subscriptionsNamesList)
            .accept(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a DELETE request with list of subscriptions names \"([^\"]*)\" to the subscription REST API \"([^\"]*)\"$")
    public void make_a_DELETE_request_with_list_of_subscriptions_names_to_the_subscription_REST_API(String subscriptionsNamesList, String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/" + subscriptionsNamesList)
            .accept(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a PUT request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_PUT_request_with_list_of_subscriptions_to_the_subscription_REST_API(String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.put(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(subscriptions.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+)$")
    public void get_response_code_of(int statusCode) throws Throwable {
        assertEquals(statusCode, mvcResult.getResponse().getStatus());
    }

    @Then("^get in response content (\\d+) found subscriptions and not found subscription name \"([^\"]*)\"$")
    public void get_in_response_content_found_subscriptions_and_not_found_subscription_name(int foundSubscriptionsNumber, String notFoundSubscriptionsName) throws Throwable {
        GetSubscriptionResponse response = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), GetSubscriptionResponse.class);
        assertEquals(foundSubscriptionsNumber, response.getFoundSubscriptions().size());
        assertEquals(notFoundSubscriptionsName, response.getNotFoundSubscriptions().get(0));
    }

    @Then("^get in response content subscription \"([^\"]*)\"$")
    public void get_in_response_content_subscription_and_reason(String subscriptionName) throws Throwable {
        JSONObject response = new JSONArray(mvcResult.getResponse().getContentAsString()).getJSONObject(0);
        assertEquals(subscriptionName, response.getString("subscription"));
    }

    @Then("^number of retrieved subscriptions using REST API \"([^\"]*)\" is (\\d+)$")
    public void number_of_retrieved_subscriptions_using_REST_API_is(String endpoint, int subscriptionsNumber) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(endpoint).accept(MediaType.APPLICATION_JSON)).andReturn();
        retrievedSubscriptions = new JSONArray(mvcResult.getResponse().getContentAsString());
        assertEquals(subscriptionsNumber, retrievedSubscriptions.length());
    }

    @Then("^retrieved subscriptions are same as given$")
    public void retrieved_subscriptions_are_same_as_given() throws Throwable {
        for (int i = 0; i < subscriptions.length(); i++) {
            assertEquals(subscriptions.getJSONObject(i).get("subscriptionName"), retrievedSubscriptions.getJSONObject(i).get("subscriptionName"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationType"), retrievedSubscriptions.getJSONObject(i).get("notificationType"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationMeta"), retrievedSubscriptions.getJSONObject(i).get("notificationMeta"));
            assertEquals(true, retrievedSubscriptions.getJSONObject(i).has("userName"));
        }
    }

}
