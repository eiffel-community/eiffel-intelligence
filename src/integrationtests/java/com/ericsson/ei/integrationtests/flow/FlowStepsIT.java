package com.ericsson.ei.integrationtests.flow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.ericsson.eiffelcommons.JenkinsManager;
import com.ericsson.eiffelcommons.helpers.JenkinsXmlData;
import com.ericsson.eiffelcommons.subscriptionobject.MailSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.SubscriptionObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import util.IntegrationTestBase;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class })
public class FlowStepsIT extends IntegrationTestBase {
    private MongoDBHandler mailhogMongoDBHandler;

    @Value("${jenkins.protocol:http}")
    public String jenkinsProtocol;
    @Value("${jenkins.host:localhost}")
    public String jenkinsHost;
    @Value("${jenkins.port:8082}")
    public int jenkinsPort;
    @Value("${jenkins.username:admin}")
    public String jenkinsUsername;
    @Value("${jenkins.password:admin}")
    public String jenkinsPassword;

    @Value("${mailhog.uri:mongodb://localhost:27017}")
    public String mailHogUri;
    @Value("${mailhog.collection:messages}")
    public String mailhogCollectionName;
    @Value("${mailhog.database:mailhog}")
    public String mailhogDatabaseName;

    private String jenkinsJobName;
    private String jenkinsJobToken;
    private String rulesFilePath;
    private String eventsFilePath;
    private String aggregatedObjectFilePath;
    private String aggregatedObjectID;
    private String upstreamInputFile;
    private ObjectMapper objectMapper = new ObjectMapper();
    private int extraEventsCount = 0;
    private long startTime;
    private JenkinsManager jenkinsManager;
    private JenkinsXmlData jenkinsXmlData;
    private SubscriptionObject subscriptionObject;
    private JSONObject jobStatusData;
    public String aggregatedEvent;

    @Given("^the rules \"([^\"]*)\"$")
    public void rules(String rulesFilePath) throws Throwable {
        this.rulesFilePath = rulesFilePath;
        aggregatedEvent = getStartEvent(this.rulesFilePath);
    }

    @Given("^the events \"([^\"]*)\"$")
    public void events(String eventsFilePath) throws Throwable {
        this.eventsFilePath = eventsFilePath;
    }

    @Given("^the resulting aggregated object \"([^\"]*)\";$")
    public void resultingAggregatedObject(String aggregatedObjectFilePath) throws Throwable {
        this.aggregatedObjectFilePath = aggregatedObjectFilePath;
    }

    @Given("^the upstream input \"([^\"]*)\"$")
    public void upstreamInput(String upstreamInputFile) throws Throwable {
        this.upstreamInputFile = upstreamInputFile;

        final URL upStreamInput = new File(upstreamInputFile).toURI().toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        extraEventsCount = upstreamJson.size();
    }

    @Given("^jenkins data is prepared$")
    public void jenkinsDataIsPrepared() throws Throwable {
        jenkinsXmlData = new JenkinsXmlData();
    }

    @Given("^subscription object of type \"([^\"]*)\" with name \"([^\"]*)\" is created$")
    public void subscriptionObjectOfTypeIsCreated(String subscriptionType,
            String subscriptionName) throws Throwable {
        if (subscriptionType.equalsIgnoreCase("Mail")) {
            subscriptionObject = new MailSubscriptionObject(subscriptionName);
        } else {
            subscriptionObject = new RestPostSubscriptionObject(subscriptionName);
        }
    }

    @When("^notification meta \"([^\"]*)\" is set in subscription$")
    public void notificationMetaIsSetInSubscription(String notificationMeta) throws Throwable {
        String notificationMetaReplaced = replaceVariablesInNotificationMeta(notificationMeta);
        subscriptionObject.setNotificationMeta(notificationMetaReplaced);
    }

    @When("^\"([^\"]*)\" authentication with username \"([^\"]*)\" and password \"([^\"]*)\" is set in subscription$")
    public void authenticationWithUsernameAndPasswordIsSetInSubscription(
            String authenticationType, String username, String password) throws Throwable {

        RestPostSubscriptionObject restPostSubscriptionObject = (RestPostSubscriptionObject) subscriptionObject;
        if (restPostSubscriptionObject != null) {
            restPostSubscriptionObject.setAuthenticationType(authenticationType)
                                      .setUsername(username)
                                      .setPassword(password);
            subscriptionObject = restPostSubscriptionObject;
        }
    }

    @When("^rest post body media type is set to \"([^\"]*)\" is set in subscription$")
    public void restPostBodyMediaTypeIsSetInSubscription(
            String restPostBodyMediaType) throws Throwable {
        subscriptionObject.setRestPostBodyMediaType(restPostBodyMediaType);
    }

    @When("^parameter form key \"([^\"]*)\" and form value \"([^\"]*)\" is added in subscription$")
    public void parameterKeyAndValueIsAddedInSubscription(String formKey, String formValue) {
        subscriptionObject.addNotificationMessageKeyValue(formKey, formValue);
    }

    @When("^condition \"([^\"]*)\" at requirement index '(\\d+)' is added in subscription$")
    public void conditionOrRequirementIsAddedInSubscription(String condition,
            int requirementIndex) throws Throwable {
        subscriptionObject.addConditionToRequirement(requirementIndex, new JSONObject().put(
                "jmespath", condition));
    }

    @When("^the eiffel events are sent$")
    public void eiffelEventsAreSent() throws Throwable {
        super.sendEventsAndConfirm(aggregatedEvent);
    }

    @When("^the upstream input events are sent")
    public void upstreamInputEventsAreSent() throws IOException {
        final URL upStreamInput = new File(upstreamInputFile).toURI().toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        if (upstreamJson != null) {
            for (JsonNode event : upstreamJson) {
                String eventStr = event.toString();
                rabbitTemplate.convertAndSend(eventStr);
            }
        }
    }

    @When("^job token \"([^\"]*)\" is added to jenkins data$")
    public void tokenIsAddedToJenkinsData(String token) throws Throwable {
        jenkinsXmlData.addJobToken(token);
        jenkinsJobToken = token;
    }

    @When("^parameter key \"([^\"]*)\" is added to jenkins data$")
    public void parameterKeyAndValueIsAddedToJenkinsData(String key) throws Throwable {
        jenkinsXmlData.addBuildParameter(key);
    }

    @When("^bash script \"([^\"]*)\" is added to jenkins data$")
    public void scriptIsAddedToJenkinsData(String bashScript) throws Throwable {
        jenkinsXmlData.addBashScript(bashScript);
    }

    @When("^jenkins job status data fetched$")
    public void jenkinsJobStatusDataFetched() throws Throwable {
        Boolean jobStatusDataFetched = false;
        long stopTime = System.currentTimeMillis() + 30000;
        while (jobStatusDataFetched == false && stopTime > System.currentTimeMillis()) {
            try {
                jobStatusData = jenkinsManager.getJenkinsBuildStatusData(this.jenkinsJobName);
                jobStatusDataFetched = true;
                break;
            } catch (Exception e) {
            }

            if (!jobStatusDataFetched) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        assertEquals("Failed to fetch Job Status Data: ", true, jobStatusDataFetched);
    }

    @Then("^the expected aggregated object ID is \"([^\"]*)\"$")
    public void expectedAggregatedObjectID(String aggregatedObjectID) throws Throwable {
        this.aggregatedObjectID = aggregatedObjectID;
    }

    @Then("^verify jenkins job data timestamp is after test subscription was created$")
    public void verifyJenkinsJobDataTimestampIsAfterTestSubscriptionWasCreated()
            throws Throwable {
        long jenkinsTriggeredTime = jobStatusData.getLong("timestamp");
        assert (jenkinsTriggeredTime >= startTime) : "Jenkins job was triggered before execution of this test.";
    }

    @Then("^jenkins job status data has key \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void jenkinsJobStatusDataHasKey(String key, String value) throws Throwable {
        String extractedValue = extractValueForKeyInJobData(key);
        assertEquals("The data jenkins recieved is not what was expected.", value, extractedValue);
    }

    @Then("^the jenkins job should be deleted$")
    public void jenkinsJobShouldBeDeleted() throws Throwable {
        jenkinsManager.deleteJob(this.jenkinsJobName);
    }

    @Then("^mongodb should contain \"([^\"]*)\" mails\\.$")
    public void mongodbShouldContainMails(int amountOfMails) throws Exception {
        long stopTime = System.currentTimeMillis() + 30000;
        Boolean mailHasBeenDelivered = false;

        while (mailHasBeenDelivered == false && stopTime > System.currentTimeMillis()) {
            JsonNode newestMailJson = getNewestMailFromDatabase();

            if (newestMailJson != null) {
                JsonNode recipients = newestMailJson.get("to");
                assertEquals("Number of recipients for test email",
                        amountOfMails, recipients.size());

                long createdDateInMillis = getDateAsEpochMillis(newestMailJson);
                mailHasBeenDelivered = createdDateInMillis >= startTime;
            }

            if (!mailHasBeenDelivered) {
                TimeUnit.SECONDS.sleep(1);
            }
        }
        assert (mailHasBeenDelivered) : "Mail was not triggered. createdDateInMillis is less than startTime.";
    }

    @Then("^jenkins is set up with job name \"([^\"]*)\"$")
    public void jenkinsIsSetUpWithJobName(String JobName) throws Throwable {
        jenkinsManager = new JenkinsManager(jenkinsProtocol, jenkinsHost, jenkinsPort,
                jenkinsUsername, jenkinsPassword);
        jenkinsManager.forceCreateJob(JobName, jenkinsXmlData.getXmlAsString());
        jenkinsJobName = JobName;
    }

    @Then("^subscription is uploaded$")
    public void subscriptionIsUploaded() throws URISyntaxException {
        assert (subscriptionObject instanceof RestPostSubscriptionObject
                || subscriptionObject instanceof MailSubscriptionObject) : "SubscriptionObject must have been initiated.";

        startTime = System.currentTimeMillis();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        postRequest.setHost(eiHost)
                   .setPort(port)
                   .setEndpoint("/subscriptions")
                   .addHeader("Content-type", "application/json")
                   .setBody(subscriptionObject.getAsSubscriptions().toString());

        ResponseEntity<String> response = postRequest.performRequest();
        assertEquals(200, response.getStatusCodeValue());
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
    protected Map<String, JsonNode> getCheckData() throws Exception {
        JsonNode expectedJSON = getJSONFromFile(aggregatedObjectFilePath);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(aggregatedObjectID, expectedJSON);
        return checkData;
    }

    @Override
    protected int extraEventsCount() {
        return extraEventsCount;
    }

    @Override
    protected List<String> getEventNamesToSend() throws IOException {
        ArrayList<String> eventNames = new ArrayList<>();

        URL eventsInput = new File(getEventsFilePath()).toURI().toURL();
        Iterator eventsIterator = objectMapper.readTree(eventsInput).fields();

        while (eventsIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) eventsIterator.next();
            eventNames.add(pair.getKey().toString());
        }

        return eventNames;
    }

    /**
     * Iterates the actions to find all the parameter objects and returns the value found if any.
     *
     * @param key
     * @return
     */
    private String extractValueForKeyInJobData(String key) {
        String value = null;
        JSONArray actions = jobStatusData.getJSONArray("actions");
        for (int i = 0; i < actions.length(); i++) {
            JSONObject actionObject = actions.getJSONObject(i);

            if (actionObject.has("parameters")) {
                JSONArray parameters = actionObject.getJSONArray("parameters");
                value = extractValueFromParameters(parameters, key);
            }

            if (value != null) {
                return value;
            }
        }
        return value;
    }

    private long getDateAsEpochMillis(JsonNode newestMailJson) {
        long createdDateInMillis = 0;
        String createdDate = newestMailJson.get("created").get("$date").asText();
        if (StringUtils.isNumeric(createdDate)) {
            createdDateInMillis = Long.parseLong(createdDate);
        } else {
            createdDateInMillis = ZonedDateTime.parse(createdDate).toInstant().toEpochMilli();
        }
        return createdDateInMillis;
    }

    /**
     * Iterates the parameter array and returns the value if the key is found in given parameter
     * array.
     *
     * @param parameters
     * @param key
     * @return
     */
    private String extractValueFromParameters(JSONArray parameters, String key) {
        String value = null;
        for (int j = 0; j < parameters.length(); j++) {
            JSONObject parameter = parameters.getJSONObject(j);

            if (parameter.has("name") && parameter.has("value") && parameter.getString("name")
                                                                            .equals(key)) {
                value = parameter.getString("value");
                return value;
            }
        }
        return value;
    }

    private JsonNode getNewestMailFromDatabase() throws Exception {
        if (mailhogMongoDBHandler == null) {
            setupMailhogMongoDBHandler();
        }
        ArrayList<String> allMails = mailhogMongoDBHandler.getAllDocuments(mailhogDatabaseName,
                mailhogCollectionName);

        if (allMails.size() > 0) {
            String mailString = allMails.get(allMails.size() - 1);
            return objectMapper.readTree(mailString);
        } else {
            return null;
        }
    }

    private void setupMailhogMongoDBHandler() {
        //MongoClientURI uri = new MongoClientURI(mailHogUri);
        //MongoClient mongoClient = new MongoClient(uri);
    	MongoClient mongoClient = MongoClients.create(mailHogUri);
        mailhogMongoDBHandler = new MongoDBHandler();
        mailhogMongoDBHandler.setMongoClient(mongoClient);
    }

    /**
     * Replaces given input parameters if user wishes with test defined parameters. If user want the
     * user may specify the host, port job name and token directly in the feauture file and they
     * will not be replaced.
     * <p>
     * ${jenkinsHost} is replaced with jenkins host.
     * <p>
     * ${jenkinsPort} is replaced with jenkins port.
     * <p>
     * ${jenkinsJobName} is replaced with lastcreated jenkins job name if any.
     * <p>
     * ${jenkinsJobToken} is replaced with last created jenkins job token if any.
     *
     * @param notificationMeta
     * @return
     */
    private String replaceVariablesInNotificationMeta(String notificationMeta) {
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsHost\\}", jenkinsHost);
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsPort\\}",
                String.valueOf(jenkinsPort));
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsJobName\\}", jenkinsJobName);
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsJobToken\\}", jenkinsJobToken);
        return notificationMeta;
    }
}
