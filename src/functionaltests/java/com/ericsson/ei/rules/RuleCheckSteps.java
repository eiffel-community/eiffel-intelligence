package com.ericsson.ei.rules;

import com.ericsson.ei.controller.RuleCheckController;
import com.ericsson.ei.utils.FunctionalTestBase;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Ignore
@AutoConfigureMockMvc
public class RuleCheckSteps extends FunctionalTestBase {

    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    @Autowired
    private RuleCheckController ruleCheckController;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    private String rules;
    private String events;

    @Given("^rules checking is enabled$")
    public void rules_checking_is_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", true);
    }

    @Given("^rules checking is not enabled$")
    public void rules_checking_is_not_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", false);
    }

    @Given("^file with JMESPath rules \"([^\"]*)\" and file with events \"([^\"]*)\"$")
    public void file_with_JMESPath_rules_and_file_with_events(String rulesFileName, String eventsFileName) throws Throwable {
        rules = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + rulesFileName), "UTF-8");
        events = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + eventsFileName), "UTF-8");
    }

    @When("^make a POST request to the REST API \"([^\"]*)\" with request parameter \"([^\"]*)\"$")
    public void make_a_POST_request_to_the_REST_API_with_request_parameter(String endpoint, String requestParam) throws Throwable {
        mvcResult = mockMvc.perform(post(endpoint)
            .param(requestParam, rules)
            .accept(MediaType.APPLICATION_JSON)
            .content(events)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a POST request to the REST API \"([^\"]*)\"$")
    public void make_a_POST_request_to_the_REST_API(String endpoint) throws Throwable {
        String requestBody = new JSONObject()
            .put("listRulesJson", new JSONArray(rules))
            .put("listEventsJson", new JSONArray(events))
            .toString();
        mvcResult = mockMvc.perform(post(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+)$")
    public void get_response_code_of(int statusCode) throws Throwable {
        assertEquals(statusCode, mvcResult.getResponse().getStatus());
    }

    @Then("^get content \"([^\"]*)\"$")
    public void get_content(String contentFileName) throws Throwable {
        String responseBody = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + contentFileName), "UTF-8");
        assertEquals(responseBody, mvcResult.getResponse().getContentAsString(), true);
    }

}