package com.ericsson.ei.integrationtests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import util.IntegrationTestBase;
import util.JenkinsManager;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class })
public class FlowStepsIT extends IntegrationTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowStepsIT.class);
    private static final String SUBSCRIPTIONS_TEMPLATE_PATH = "src/integrationtests/resources/subscriptionsTemplate.json";

    private String jenkins_job_name;
    private String rulesFilePath;
    private String eventsFilePath;
    private String aggregatedObjectFilePath;
    private String aggregatedObjectID;
    private String upstreamInputFile;
    private ObjectMapper objectMapper = new ObjectMapper();
    private int extraEventsCount = 0;

    private long startTime;

    @Value( "${jenkins.host:localhost}")
    private String JENKINS_HOST;

    @Value( "${jenkins.port:8081}")
    private int JENKINS_PORT;

    @Value( "${jenkins.username:admin}")
    private String JENKINS_USERNAME;

    @Value( "${jenkins.password:admin}")
    private String JENKINS_PASSWORD;

    JenkinsManager jenkinsManager;

    @Given("^that \"([^\"]*)\" subscription with jmespath \"([^\"]*)\" is uploaded$")
    public void that_subscription_with_jmespath_is_uploaded(String subscriptionType, String JmesPath) throws URISyntaxException, IOException {
        startTime = System.currentTimeMillis();

        URL subscriptionsInput = new File(SUBSCRIPTIONS_TEMPLATE_PATH).toURI().toURL();
        ArrayNode subscriptionsJson = (ArrayNode) objectMapper.readTree(subscriptionsInput);

        if(subscriptionType.equals("REST/POST")) {
            subscriptionsJson = setSubscriptionRestPostFieldsWithJmesPath(subscriptionsJson, JmesPath);
        } else if (subscriptionType.equals("mail")) {
            subscriptionsJson = setSubscriptionMailFieldsWithJmesPath(subscriptionsJson, JmesPath);
        }

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        ResponseEntity response = postRequest.setHost(eiHost)
                .setPort(port)
                .setEndpoint("/subscriptions")
                .addHeader("Content-type", "application/json")
                .setBody(subscriptionsJson.toString())
                .performRequest();
        assertEquals(200, response.getStatusCodeValue());
    }

    @Given("^jenkins is set up with a job \"([^\"]*)\"$")
   public void jenkins_is_set_up_with_a_job(String jenkins_job_name) throws Throwable {
        jenkinsManager = new JenkinsManager(JENKINS_HOST, JENKINS_PORT, JENKINS_USERNAME, JENKINS_PASSWORD);
        String xmlJobData = jenkinsManager.getXmlJobData("123", "");
        jenkinsManager.createJob(jenkins_job_name, xmlJobData);

        this.jenkins_job_name = jenkins_job_name;
    }

    @Given("^the rules \"([^\"]*)\"$")
    public void the_rules(String rulesFilePath) throws Throwable {
        this.rulesFilePath = rulesFilePath;
    }

    @Given("^the events \"([^\"]*)\"$")
    public void the_events(String eventsFilePath) throws Throwable {
        this.eventsFilePath = eventsFilePath;
    }

    @Given("^the resulting aggregated object \"([^\"]*)\";$")
    public void the_resulting_aggregated_object(String aggregatedObjectFilePath) throws Throwable {
        this.aggregatedObjectFilePath = aggregatedObjectFilePath;
    }

    @Given("^the expected aggregated object ID is \"([^\"]*)\"$")
    public void the_expected_aggregated_object_ID_is(String aggregatedObjectID) throws Throwable {
        this.aggregatedObjectID = aggregatedObjectID;
    }

    @Given("^the upstream input \"([^\"]*)\"$")
    public void the_upstream_input(String upstreamInputFile) throws Throwable {
        this.upstreamInputFile = upstreamInputFile;

        final URL upStreamInput = new File(upstreamInputFile).toURI().toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        extraEventsCount = upstreamJson.size();
    }

    @When("^the eiffel events are sent$")
    public void eiffel_events_are_sent() throws Throwable  {
        super.sendEventsAndConfirm();
    }

    @When("^the upstream input events are sent")
    public void upstream_input_events_are_sent() throws IOException {
        final URL upStreamInput = new File(upstreamInputFile).toURI().toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        if (upstreamJson != null) {
            for (JsonNode event : upstreamJson) {
                String eventStr = event.toString();
                rabbitTemplate.convertAndSend(eventStr);
            }
        }
    }

    @Then("^the jenkins job should have been triggered\\.$")
    public void the_jenkins_job_should_have_been_triggered() throws Throwable {
        long stopTime = System.currentTimeMillis() + 30000;
        Boolean jobHasBeenTriggered = false;
        while(jobHasBeenTriggered == false && stopTime > System.currentTimeMillis()) {
            jobHasBeenTriggered = jenkinsManager.jobHasBeenTriggered(this.jenkins_job_name);

            if(!jobHasBeenTriggered) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        assertEquals(true, jobHasBeenTriggered);
        jenkinsManager.deleteJob(this.jenkins_job_name);
    }

    @Then("^mongodb should contain mail\\.$")
    public void mongodb_should_contain_mails() throws Throwable {
        long stopTime = System.currentTimeMillis() + 30000;
        Boolean mailHasBeenDelivered = false;
        long createdDateInMillis = 0;

        while(mailHasBeenDelivered == false && stopTime > System.currentTimeMillis()) {
            JsonNode newestMailJson = getNewestMailFromDatabase();
            String createdDate = newestMailJson.get("created").get("$date").asText();

            createdDateInMillis = ZonedDateTime.parse(createdDate).toInstant().toEpochMilli();
            mailHasBeenDelivered = createdDateInMillis >= startTime;
            if (!mailHasBeenDelivered) {
                TimeUnit.SECONDS.sleep(1);
            }
        }
        assert(createdDateInMillis >= startTime): "Mail was not triggered. createdDateInMillis is less than startTime.";
    }

    @Override
    protected String getRulesFilePath() {
        return rulesFilePath;
    }

    @Override
    protected String getEventsFilePath() {
        return eventsFilePath;
    }

    @Override
    protected Map<String, JsonNode> getCheckData() throws IOException {
        JsonNode expectedJSON = getJSONFromFile(aggregatedObjectFilePath);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(aggregatedObjectID, expectedJSON);
        return checkData;
    }

    @Override
    protected int extraEventsCount() {
        return extraEventsCount;
    }

    private JsonNode getNewestMailFromDatabase() throws Exception {
        ArrayList<String> allMails = mongoDBHandler.getAllDocuments(MAILHOG_DATABASE_NAME, "messages");

        if(allMails.size() > 0) {
            String mailString = allMails.get(allMails.size() - 1);
            return objectMapper.readTree(mailString);

        } else {
            throw new Exception("No mails found.");
        }
    }

    /**
     * Sets the subscription template with necessary fields for a REST/POST subscription
     * @param subscriptionJson - An arraynode with the subscription that should be updated
     * @param JmesPath - A jmesPath expression with the required condition for the subscription to be triggered
     * @return an arraynode with the updated subscription
     */
    private ArrayNode setSubscriptionRestPostFieldsWithJmesPath(ArrayNode subscriptionJson, String jmesPath) {
        ObjectNode subscriptionJsonObject = ((ObjectNode) subscriptionJson.get(0));

        subscriptionJsonObject.put("userName", JENKINS_USERNAME);
        subscriptionJsonObject.put("password", JENKINS_PASSWORD);
        subscriptionJsonObject.put("authenticationType", "BASIC_AUTH");
        subscriptionJsonObject.put("restPostBodyMediaType", "application/x-www-form-urlencoded");
        subscriptionJsonObject.put("notificationType", "REST_POST");
        subscriptionJsonObject.put("notificationMeta", "http://" + JENKINS_HOST + ":" + JENKINS_PORT + "/job/" + this.jenkins_job_name +"/build?token='123'");

        ObjectNode requirement = ((ObjectNode) subscriptionJsonObject.get("requirements").get(0).get("conditions").get(0));
        requirement.put("jmespath", jmesPath);

        ObjectNode notificationMessageKeyValue = ((ObjectNode) subscriptionJsonObject.get("notificationMessageKeyValues").get(0));
        notificationMessageKeyValue.put("formkey", "test");

        return subscriptionJson;
    }

    /**
     * Sets the subscription template with necessary fields for a MAIL subscription
     * @param subscriptionJson - An arraynode with the subscription that should be updated
     * @param JmesPath - A jmesPath expression with the required condition for the subscription to be triggered
     * @return an arraynode with the updated subscription
     */
    private ArrayNode setSubscriptionMailFieldsWithJmesPath(ArrayNode subscriptionJson, String JmpesPath) {
        ObjectNode subscriptionJsonObject = ((ObjectNode) subscriptionJson.get(0));
        subscriptionJsonObject.put("restPostBodyMediaType", "application/json");

        ObjectNode requirement = ((ObjectNode) subscriptionJson.get(0).get("requirements").get(0).get("conditions").get(0));
        requirement.put("jmespath", JmpesPath);

        return subscriptionJson;
    }
}
