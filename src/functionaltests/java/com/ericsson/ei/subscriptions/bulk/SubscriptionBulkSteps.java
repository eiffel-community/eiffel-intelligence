package com.ericsson.ei.subscriptions.bulk;

import com.ericsson.ei.utils.FunctionalTestBase;
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

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionBulkSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionBulkSteps.class);

    private static final String TEST_RESOURCES_PATH = "src/functionaltests/resources/subscriptions/";

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private JSONArray subscriptions;

    @Given("^file with subscriptions \"([^\"]*)\"$")
    public void file_with_subscriptions(String subscriptionsFileName) throws Throwable {
        String fileContent = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + subscriptionsFileName), "UTF-8");
        subscriptions = new JSONArray(fileContent);
    }

    @When("^make a POST request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_POST_request_with_list_of_subscriptions_to_the_subscription_REST_API(String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(post(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(subscriptions.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a PUT request with list of subscriptions to the subscription REST API \"([^\"]*)\"$")
    public void make_a_PUT_request_with_list_of_subscriptions_to_the_subscription_REST_API(String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(put(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(subscriptions.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+) and retrieved subscriptions using REST API \"([^\"]*)\" are same as given$")
    public void get_response_code_of_and_retrieved_subscriptions_using_REST_API_are_same_as_given(
        int statusCode, String endpoint) throws Throwable {
        assertEquals(statusCode, mvcResult.getResponse().getStatus());

        mvcResult = mockMvc.perform(get(endpoint).accept(MediaType.APPLICATION_JSON)).andReturn();

        JSONArray retrievedSubscriptions = new JSONArray(mvcResult.getResponse().getContentAsString());
        for (int i = 0; i < subscriptions.length(); i++) {
            assertEquals(subscriptions.getJSONObject(i).get("subscriptionName"), retrievedSubscriptions.getJSONObject(i).get("subscriptionName"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationType"), retrievedSubscriptions.getJSONObject(i).get("notificationType"));
            assertEquals(subscriptions.getJSONObject(i).get("notificationMeta"), retrievedSubscriptions.getJSONObject(i).get("notificationMeta"));
            assertEquals(true, retrievedSubscriptions.getJSONObject(i).has("userName"));
        }
    }

}
