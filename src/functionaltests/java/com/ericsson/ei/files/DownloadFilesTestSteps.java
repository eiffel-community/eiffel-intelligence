package com.ericsson.ei.files;

import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
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
    private HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
    ResponseEntity<String> response;
    
    @LocalServerPort
    private int applicationPort;
    private String hostName = getHostName();


    @Given("^Eiffel Intelligence instance is up and running$")
    public void eiffel_intelligence_instance_is_up_and_running() throws Exception {
        LOGGER.debug("Checking if Eiffel Intelligence instance is up and running.");
        httpRequest.setHost(hostName)
            .setPort(applicationPort)
            .addHeader("Content-type:", MediaType.APPLICATION_JSON_VALUE.toString())
            .setEndpoint("/subscriptions");

        response = httpRequest.performRequest();
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
    
    @Then("^List available files$")
    public void list_available_files() throws Exception {
        LOGGER.debug("Listing all availble files that can be download via RestApi.");
        String expectedSubscriptionsValue =  "/download/subscriptionsTemplate";

        httpRequest.setEndpoint("/download");
        response = httpRequest.performRequest();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String actualSubscriptionsValue = objMapper.readValue(response.getBody(), JsonNode.class).get("subscriptions").asText();
        assertEquals("List all files don't return expected subscriptions file value. \nExpected: "
        + expectedSubscriptionsValue + "\nActual: "+ actualSubscriptionsValue,
        expectedSubscriptionsValue, actualSubscriptionsValue);
    }
    
    @And("^Get subscription template file$")
    public void get_subscription_template_file() throws Exception {
        String expectedSubscriptionTemplateContent = FileUtils.readFileToString(new File(SUBSCRIPTIONS_TEMPLATE_FILEPATH), "UTF-8");

        httpRequest.setEndpoint("/download/subscriptionsTemplate");
        response = httpRequest.performRequest();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String actualSubscriptionTemplateContent = response.getBody();
        assertEquals("Get SubscriptionTemplate file failed or contents is not as expected. \nExpected: "
        + expectedSubscriptionTemplateContent + "\nActual: "+ actualSubscriptionTemplateContent,
        expectedSubscriptionTemplateContent, actualSubscriptionTemplateContent);
    }
    
    @And("^Get rules template file$")
    public void get_rules_template_file() throws Exception {
        String expectedRulesTemplateContent = FileUtils.readFileToString(new File(RULES_TEMPLATE_FILEPATH), "UTF-8");
        
        httpRequest.setEndpoint("/download/rulesTemplate");
        response = httpRequest.performRequest();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String actualRulesTemplateContent = response.getBody();
        assertEquals("Get RulesTemplate file failed or contents is not as expected. \nExpected: "
        + expectedRulesTemplateContent + "\nActual: "+ actualRulesTemplateContent,
        expectedRulesTemplateContent, actualRulesTemplateContent);
    }
    
    @And("^Get event template file$")
    public void get_event_template_file() throws Exception {
        String expectedEventsTemplateContent = FileUtils.readFileToString(new File(EVENTS_TEMPLATE_FILEPATH), "UTF-8");

        httpRequest.setEndpoint("/download/eventsTemplate");
        response = httpRequest.performRequest();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String actualEventsTemplateContent = response.getBody();
        assertEquals("Get EventsTemplate file failed or contents is not as expected. \nExpected: "
        + expectedEventsTemplateContent + "\nActual: "+ actualEventsTemplateContent,
        expectedEventsTemplateContent, actualEventsTemplateContent);
    }
}
