package com.ericsson.ei.files;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@Ignore
@AutoConfigureMockMvc
public class DownloadFilesTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadFilesTestSteps.class);
    
    private static final String SUBSCRIPTIONS_TEMPLATE_FILEPATH = "src/main/resources/templates/subscriptionsTemplate.json";
    private static final String RULES_TEMPLATE_FILEPATH = "src/main/resources/templates/rulesTemplate.json";
    private static final String EVENTS_TEMPLATE_FILEPATH = "src/main/resources/templates/eventsTemplate.json";
    
    private ObjectMapper objMapper = new ObjectMapper();
    
    @Autowired
    private MockMvc mockMvc;
    private MvcResult mvcResult;
    
    @Given("^Eiffel Intelligence instance is up and running$")
    public void eiffel_intelligence_instance_is_up_and_running() throws Exception {
        LOGGER.debug("Checking if Eiffel Intelligence instance is up and running.");
        mockMvc.perform(MockMvcRequestBuilders.get("/subscriptions")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }
    
    @Then("^List available files$")
    public void list_available_files() throws Exception {
        LOGGER.debug("Listing all availble files that can be download via RestApi.");
        String expectedSubscriptionsValue =  "/download/subscriptionsTemplate";
        
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/download")
                    .accept(MediaType.APPLICATION_JSON_VALUE))
                    .andReturn();
        String actualSubscriptionsValue = objMapper.readValue(mvcResult.getResponse().getContentAsString(), JsonNode.class).get("subscriptions").asText();
        assertEquals("List all files don't return expected subscriptions file value. \nExpected: "
        + expectedSubscriptionsValue + "\nActual: "+ actualSubscriptionsValue,
        actualSubscriptionsValue, expectedSubscriptionsValue);
    }
    
    @And("^Get subscription template file$")
    public void get_subscription_template_file() throws Exception {
        String expectedSubscriptionTemplateContent = FileUtils.readFileToString(new File(SUBSCRIPTIONS_TEMPLATE_FILEPATH), "UTF-8");
        
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/download/subscriptionsTemplate")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        String actualSubscriptionTemplateContent = mvcResult.getResponse().getContentAsString();
        assertEquals("Get SubscriptionTemplate file failed or contents is not as expected. \nExpected: "
        + expectedSubscriptionTemplateContent + "\nActual: "+ actualSubscriptionTemplateContent,
        actualSubscriptionTemplateContent, expectedSubscriptionTemplateContent);
    }
    
    @And("^Get rules template file$")
    public void get_rules_template_file() throws Exception {
        String expectedRulesTemplateContent = FileUtils.readFileToString(new File(RULES_TEMPLATE_FILEPATH), "UTF-8");
        
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/download/rulesTemplate")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        String actualRulesTemplateContent = mvcResult.getResponse().getContentAsString();
        assertEquals("Get RulesTemplate file failed or contents is not as expected. \nExpected: "
        + expectedRulesTemplateContent + "\nActual: "+ actualRulesTemplateContent,
        actualRulesTemplateContent, expectedRulesTemplateContent);
    }
    
    @And("^Get event template file$")
    public void get_event_template_file() throws Exception {
        String expectedEventsTemplateContent = FileUtils.readFileToString(new File(EVENTS_TEMPLATE_FILEPATH), "UTF-8");
        
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/download/eventsTemplate")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        String actualEventsTemplateContent = mvcResult.getResponse().getContentAsString();
        assertEquals("Get EventsTemplate file failed or contents is not as expected. \nExpected: "
        + expectedEventsTemplateContent + "\nActual: "+ actualEventsTemplateContent,
        actualEventsTemplateContent, expectedEventsTemplateContent);
    }
}
