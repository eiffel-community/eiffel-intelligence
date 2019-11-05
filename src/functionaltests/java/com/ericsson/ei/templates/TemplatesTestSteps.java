package com.ericsson.ei.templates;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@Ignore
@TestPropertySource(properties = {"spring.data.mongodb.database: TemplatesTestSteps",
        "rabbitmq.exchange.name: TemplatesTestSteps-exchange",
        "rabbitmq.consumerName: TemplatesTestStepsConsumer" })
@AutoConfigureMockMvc
public class TemplatesTestSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatesTestSteps.class);

    private static final String SUBSCRIPTIONS_TEMPLATE_FILEPATH = "src/main/resources/templates/subscriptions.json";
    private static final String RULES_TEMPLATE_FILEPATH = "src/main/resources/templates/rules.json";
    private static final String EVENTS_TEMPLATE_FILEPATH = "src/main/resources/templates/events.json";

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
                   .addHeader("Content-type:", MediaType.APPLICATION_JSON_VALUE)
                   .setEndpoint("/subscriptions");

        response = httpRequest.performRequest();
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Then("^List available files$")
    public void list_available_files() throws Exception {
        LOGGER.debug("Listing all available template files that can be downloaded via REST API.");
        String expectedSubscriptionsValue = "/templates/subscriptions";

        httpRequest.setEndpoint("/templates");
        response = httpRequest.performRequest();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String actualSubscriptionsValue = objMapper.readValue(response.getBody(), JsonNode.class).get("subscriptions").asText();
        assertEquals("Template endpoints did not return expected subscriptions endpoint.",
                expectedSubscriptionsValue, actualSubscriptionsValue);
    }

    @And("^Get subscription template file$")
    public void get_subscription_template_file() throws Exception {
        String expectedSubscriptionTemplateContent = FileUtils.readFileToString(
                new File(SUBSCRIPTIONS_TEMPLATE_FILEPATH), "UTF-8");

        httpRequest.setEndpoint("/templates/subscriptions");
        response = httpRequest.performRequest();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String actualSubscriptionTemplateContent = response.getBody();
        assertEquals("Failed to get template file for subscriptions. ",
            expectedSubscriptionTemplateContent, actualSubscriptionTemplateContent);
    }

    @And("^Get rules template file$")
    public void get_rules_template_file() throws Exception {
        String expectedRulesTemplateContent = FileUtils.readFileToString(new File(RULES_TEMPLATE_FILEPATH), "UTF-8");

        httpRequest.setEndpoint("/templates/rules");
        response = httpRequest.performRequest();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String actualRulesTemplateContent = response.getBody();
        assertEquals("Failed to get template file for rules.",
                expectedRulesTemplateContent, actualRulesTemplateContent);
    }

    @And("^Get event template file$")
    public void get_event_template_file() throws Exception {
        String expectedEventsTemplateContent = FileUtils.readFileToString(new File(EVENTS_TEMPLATE_FILEPATH), "UTF-8");

        httpRequest.setEndpoint("/templates/events");
        response = httpRequest.performRequest();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String actualEventsTemplateContent = response.getBody();
        assertEquals("Failed to get template file for events.",
                expectedEventsTemplateContent, actualEventsTemplateContent);
    }
}
