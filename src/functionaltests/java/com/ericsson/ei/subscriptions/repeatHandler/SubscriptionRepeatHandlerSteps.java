package com.ericsson.ei.subscriptions.repeatHandler;

import com.ericsson.ei.services.SubscriptionService;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionRepeatHandlerSteps extends FunctionalTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRepeatHandlerSteps.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionService subscriptionService;

    private MvcResult result;

    @Override
    protected int extraEventsCount() {
        return super.extraEventsCount();
    }

    @Override
    protected String getRulesFilePath() {
        return super.getRulesFilePath();
    }

    @Override
    protected String getEventsFilePath() {
        return super.getEventsFilePath();
    }

    @Override
    protected List<String> getEventNamesToSend() {
        return super.getEventNamesToSend();
    }

    @Override
    protected Map<String, JsonNode> getCheckData() {
        return super.getCheckData();
    }

    @Given("^Subscription that will match this Aggregated Object$")
    public void subscription_that_will_match_this_Aggregated_Object() {
        LOGGER.debug("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @When("^Publish events on MessageBus$")
    public void send_events() {
        LOGGER.debug("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @Then("^Subscription should only match Aggregated Object only one time$")
    public void subscription_should_only_match_Aggregated_Object_only_one_time() {

    }

    @Given("^The REST API  \"([^\"]*)\"  is up and running$")
    public void subscription_mail_trigger_is_created(String subscriptionEndPoint) {
        try {
            result = mockMvc.perform(MockMvcRequestBuilders.get(subscriptionEndPoint).accept(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name, String subscriptionEndPoint) {
        try {
            result = mockMvc.perform(MockMvcRequestBuilders.delete(subscriptionEndPoint + name).accept(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Then("^Check in MongoDB that subscription has been removed and check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_that_subscription_has_been_removed() {

    }

    @Then("^Check in MongoDB RepeatFlagHandler collection that the subscription has been removed$")
    public void check_in_MongoDB_RepeatFlagHandler_collection_that_the_subscription_has_been_removed() {

    }

    @Then("^Subscription should match Aggregated Object at least two times$")
    public void subscription_should_only_match_AggregatedObject_at_least_two_times() {

    }
}