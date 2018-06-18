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

    private static final String TEST_RESOURCES_PATH = "src/functionaltests/resources/rules/";

    @Autowired
    private RuleCheckController ruleCheckController;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mvcResult;

    @Given("^rules checking is enabled$")
    public void rules_checking_is_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", true);
    }

    @Given("^rules checking is not enabled$")
    public void rules_checking_is_not_enabled() throws Throwable {
        ReflectionTestUtils.setField(ruleCheckController, "testEnable", false);
    }

    @When("^make a POST request with JMESPath rule \"([^\"]*)\" and JSON object \"([^\"]*)\" to the REST API \"([^\"]*)\"$")
    public void make_a_POST_request_with_JMESPath_rule_and_JSON_object_to_the_REST_API(String ruleFileName, String eventFileName, String endpoint) throws Throwable {
        String extractionRules = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + ruleFileName), "UTF-8");
        String requestBody = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + eventFileName), "UTF-8");
        mvcResult = mockMvc.perform(post(endpoint)
            .param("rule", extractionRules)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @When("^make a POST request with list of JMESPath rules \"([^\"]*)\" and list of JSON objects \"([^\"]*)\" to the REST API \"([^\"]*)\"$")
    public void make_a_POST_request_with_list_of_JMESPath_rules_and_list_of_JSON_objects_to_the_REST_API(String rulesFileName, String eventsFileName, String endpoint) throws Throwable {
        String rules = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + rulesFileName), "UTF-8");
        String events = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + eventsFileName), "UTF-8");
        String requestBody = new JSONObject().put("listRulesJson", new JSONArray(rules)).put("listEventsJson", new JSONArray(events)).toString();
        System.out.println(requestBody);
        mvcResult = mockMvc.perform(post(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+) and content \"([^\"]*)\"$")
    public void get_response_code_of_and_content(int statusCode, String contentFileName) throws Throwable {
        String responseBody = FileUtils.readFileToString(new File(TEST_RESOURCES_PATH + contentFileName), "UTF-8");
        assertEquals(statusCode, mvcResult.getResponse().getStatus());
        assertEquals(responseBody, mvcResult.getResponse().getContentAsString(), true);
    }

}