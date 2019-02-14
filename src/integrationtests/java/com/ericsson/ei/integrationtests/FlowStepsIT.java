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
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private static final String SUBSCRIPTIONS_TEMPLATE_PATH = "src/integrationtests/resources/subscriptionsTemplate222222222222222222.json";
    private static final String JENKINS_TOKEN = "123";

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

    @Value("${jenkins.host:http}")
    private String jenkinsProtocol;

    @Value("${jenkins.host:localhost}")
    private String jenkinsHost;

    @Value("${jenkins.port:8081}")
    private int jenkinsPort;

    @Value("${jenkins.username:admin}")
    private String jenkinsUsername;

    @Value("${jenkins.password:admin}")
    private String jenkinsPassword;

    private JenkinsManager jenkinsManager;
    private JenkinsXmlData jenkinsXmlData;
    private SubscriptionObject subscriptionObject;
    private JSONObject jobStatusData;

    @Given("^that \"([^\"]*)\" subscription with jmespath \"([^\"]*)\" is uploaded$")
    public void that_subscription_with_jmespath_is_uploaded(String subscriptionType, String JmesPath)
            throws URISyntaxException, IOException {
        startTime = System.currentTimeMillis();

        URL subscriptionsInput = new File(SUBSCRIPTIONS_TEMPLATE_PATH).toURI()
                                                                      .toURL();
        ArrayNode subscriptionsJson = (ArrayNode) objectMapper.readTree(subscriptionsInput);

        if (subscriptionType.equals("REST/POST")) {
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
        jenkinsManager = new JenkinsManager(jenkinsProtocol, jenkinsHost, jenkinsPort, jenkinsUsername,
                jenkinsPassword);
        JenkinsXmlData jenkinsXmlData = new JenkinsXmlData();
        jenkinsXmlData.addJobToken(JENKINS_TOKEN)
                      .addBashScript("echo Success");
        jenkinsManager.forceCreateJob(jenkins_job_name, jenkinsXmlData.getXmlAsString());

        this.jenkinsJobName = jenkins_job_name;
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

    @Given("^the upstream input \"([^\"]*)\"$")
    public void the_upstream_input(String upstreamInputFile) throws Throwable {
        this.upstreamInputFile = upstreamInputFile;

        final URL upStreamInput = new File(upstreamInputFile).toURI()
                                                             .toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        extraEventsCount = upstreamJson.size();
    }

    @Given("^jenkins data is prepared$")
    public void jenkins_data_is_prepared() throws Throwable {
        jenkinsXmlData = new JenkinsXmlData();
    }

    @Given("^subscription object for \"([^\"]*)\" with name \"([^\"]*)\" is created$")
    public void subscription_object_for_with_name_is_created(String subscriptionType, String subscriptionName)
            throws Throwable {
        if (subscriptionType.equalsIgnoreCase("Mail")) {
            subscriptionObject = new MailSubscriptionObject(subscriptionName);
        } else {
            subscriptionObject = new RestPostSubscriptionObject(subscriptionName);
        }
    }

    @Given("^all previous steps passed$")
    public void all_previous_steps_passed() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
    }

    @When("^notification meta \"([^\"]*)\" is set in subscription$")
    public void notification_meta_is_set_in_subscription(String notificationMeta) throws Throwable {
        notificationMeta = replaceVariablesInNotificationMeta(notificationMeta);
        subscriptionObject.setNotificationMeta(notificationMeta);
    }

    @When("^basic_auth authentication with username \"([^\"]*)\" and password \"([^\"]*)\" is set in subscription$")
    public void basic_auth_authentication_with_username_and_password_is_set_in_subscription(String username,
                                                                                            String password)
            throws Throwable {
        if (subscriptionObject instanceof RestPostSubscriptionObject) {
            ((RestPostSubscriptionObject) subscriptionObject).setBasicAuth(username, password);
        }
    }

    @When("^rest post body media type is set to \"([^\"]*)\" is set in subscription$")
    public void rest_post_body_media_type_is_set_to_is_set_in_subscription(String restPostBodyMediaType)
            throws Throwable {
        subscriptionObject.setRestPostBodyMediaType(restPostBodyMediaType);

    }

    @When("^paremeter form key \"([^\"]*)\" and form value \"([^\"]*)\" is added in subscription$")
    public void paremeter_key_and_value_is_added_in_subscription(String formKey, String formValue) {
        subscriptionObject.addNotificationMessageKeyValue(formKey, formValue);
    }

    @When("^condition \"([^\"]*)\" at requirement index '(\\d+)' is added in subscription$")
    public void requirement_for_condition_is_added_in_subscription(String condition, int requirementIndex)
            throws Throwable {
        subscriptionObject.addConditionToRequirement(requirementIndex, new JSONObject().put("jmespath", condition));
    }

    @When("^the eiffel events are sent$")
    public void eiffel_events_are_sent() throws Throwable {
        super.sendEventsAndConfirm();
        //System.out.println("Port ::::: " + port);
        //TimeUnit.SECONDS.sleep(120);
    }

    @When("^the upstream input events are sent")
    public void upstream_input_events_are_sent() throws IOException {
        final URL upStreamInput = new File(upstreamInputFile).toURI()
                                                             .toURL();
        ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
        if (upstreamJson != null) {
            for (JsonNode event : upstreamJson) {
                String eventStr = event.toString();
                rabbitTemplate.convertAndSend(eventStr);
            }
        }
    }

    @When("^job token \"([^\"]*)\" is added to jenkins data$")
    public void token_is_added_to_jenkins_data(String token) throws Throwable {
        jenkinsXmlData.addJobToken(token);
        jenkinsJobToken = token;
    }

    @When("^parameter key \"([^\"]*)\" is added to jenkins data$")
    public void parameter_key_and_value_is_added_to_jenkins_data(String key) throws Throwable {
        jenkinsXmlData.addBuildParameter(key);
    }

    @When("^bash script \"([^\"]*)\" is added to jenkins data$")
    public void script_is_added_to_jenkins_data(String bashScript) throws Throwable {
        jenkinsXmlData.addBashScript(bashScript);
    }

    @When("^jenkins job status data fetched$")
    public void the_jenkins_job_should_have_been_triggered() throws Throwable {
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

        assertEquals(true, jobStatusDataFetched);
    }

    @Then("^the expected aggregated object ID is \"([^\"]*)\"$")
    public void the_expected_aggregated_object_ID_is(String aggregatedObjectID) throws Throwable {
        this.aggregatedObjectID = aggregatedObjectID;
    }

    @Then("^verify jenkins job data timestamp is after test subscription was creted$")
    public void verify_jenkins_job_data_timestamp_is_after_test_subscription_was_creted() throws Throwable {
        long jenkinsTriggeredTime = jobStatusData.getLong("timestamp");
        assert (jenkinsTriggeredTime >= startTime) : "Jenkins job was triggered before execution of this test.";
    }

    @Then("^jenkins job status data has key \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void jenkins_job_status_data_has_key(String key, String value) throws Throwable {
        String extractedValue = extractValueForKeyInJobData(key);
        assertEquals("The data jenkins recieved is not what was expected.", value, extractedValue);
    }

    @Then("^the jenkins job should be deleted$")
    public void the_jenkins_job_should_be_deleted() throws Throwable {
        jenkinsManager.deleteJob(this.jenkinsJobName);
    }

    @Then("^mongodb should contain mail\\.$")
    public void mongodb_should_contain_mails() throws Throwable {
        long stopTime = System.currentTimeMillis() + 30000;
        Boolean mailHasBeenDelivered = false;
        long createdDateInMillis = 0;

        while (mailHasBeenDelivered == false && stopTime > System.currentTimeMillis()) {
            JsonNode newestMailJson = getNewestMailFromDatabase();

            if (newestMailJson != null) {
                String createdDate = newestMailJson.get("created")
                                                   .get("$date")
                                                   .asText();

                createdDateInMillis = ZonedDateTime.parse(createdDate)
                                                   .toInstant()
                                                   .toEpochMilli();
                mailHasBeenDelivered = createdDateInMillis >= startTime;
            }

            if (!mailHasBeenDelivered) {
                TimeUnit.SECONDS.sleep(1);
            }
        }
        assert (mailHasBeenDelivered) : "Mail was not triggered. createdDateInMillis is less than startTime.";
    }

    @Then("^jenkins is set up with job name \"([^\"]*)\"$")
    public void jenkins_is_set_up_with_job_name(String JobName) throws Throwable {
        jenkinsManager = new JenkinsManager(jenkinsProtocol, jenkinsHost, jenkinsPort, jenkinsUsername,
                jenkinsPassword);
        jenkinsManager.forceCreateJob(JobName, jenkinsXmlData.getXmlAsString());
        jenkinsJobName = JobName;
    }

    @Then("^subscription is uploaded$")
    public void subscription_is_uploaded() throws URISyntaxException {
        assert (subscriptionObject instanceof RestPostSubscriptionObject
                || subscriptionObject instanceof MailSubscriptionObject) : "SubscriptionObject must have been initiated.";

        startTime = System.currentTimeMillis();

        HttpRequest postRequest = new HttpRequest(HttpMethod.POST);
        postRequest.setHost(eiHost)
                   .setPort(port)
                   .setEndpoint("/subscriptions")
                   .addHeader("Content-type", "application/json")
                   .setBody(subscriptionObject.getAsSubscriptions()
                                              .toString());

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

    /**
     * Iterates the parameter array and returns the value if the key is found in given parameter array.
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
        ArrayList<String> allMails = mongoDBHandler.getAllDocuments(MAILHOG_DATABASE_NAME, "messages");

        if (allMails.size() > 0) {
            String mailString = allMails.get(allMails.size() - 1);
            return objectMapper.readTree(mailString);
        } else {
            return null;
        }
    }

    /**
     * Replaces given input parameters if user wishes with test defined parameters. If user want the user may specify
     * the host, port job name and token directly in the feauture file and they will not be replaced.
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
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsPort\\}", String.valueOf(jenkinsPort));
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsJobName\\}", this.jenkinsJobName);
        notificationMeta = notificationMeta.replaceAll("\\$\\{jenkinsJobToken\\}", this.jenkinsJobToken);
        return notificationMeta;
    }

    /**
     * Sets the subscription template with necessary fields for a REST/POST subscription
     *
     * @param subscriptionJson
     *            - An arraynode with the subscription that should be updated
     * @param JmesPath
     *            - A jmesPath expression with the required condition for the subscription to be triggered
     * @return an arraynode with the updated subscription
     */
    private ArrayNode setSubscriptionRestPostFieldsWithJmesPath(ArrayNode subscriptionJson, String jmesPath) {
        ObjectNode subscriptionJsonObject = ((ObjectNode) subscriptionJson.get(0));

        subscriptionJsonObject.put("userName", jenkinsUsername);
        subscriptionJsonObject.put("password", jenkinsPassword);
        subscriptionJsonObject.put("authenticationType", "BASIC_AUTH");
        subscriptionJsonObject.put("restPostBodyMediaType", "application/x-www-form-urlencoded");
        subscriptionJsonObject.put("notificationType", "REST_POST");
        subscriptionJsonObject.put("notificationMeta", "http://" + jenkinsHost + ":" + jenkinsPort + "/job/"
                + this.jenkinsJobName + "/build?token='" + JENKINS_TOKEN + "'");

        ObjectNode requirement = ((ObjectNode) subscriptionJsonObject.get("requirements")
                                                                     .get(0)
                                                                     .get("conditions")
                                                                     .get(0));
        requirement.put("jmespath", jmesPath);

        ObjectNode notificationMessageKeyValue = ((ObjectNode) subscriptionJsonObject.get(
                "notificationMessageKeyValues")
                                                                                     .get(0));
        notificationMessageKeyValue.put("formkey", "json");

        return subscriptionJson;
    }

    /**
     * Sets the subscription template with necessary fields for a MAIL subscription
     *
     * @param subscriptionJson
     *            - An arraynode with the subscription that should be updated
     * @param JmesPath
     *            - A jmesPath expression with the required condition for the subscription to be triggered
     * @return an arraynode with the updated subscription
     */
    private ArrayNode setSubscriptionMailFieldsWithJmesPath(ArrayNode subscriptionJson, String JmpesPath) {
        ObjectNode subscriptionJsonObject = ((ObjectNode) subscriptionJson.get(0));
        subscriptionJsonObject.put("restPostBodyMediaType", "application/json");

        ObjectNode requirement = ((ObjectNode) subscriptionJson.get(0)
                                                               .get("requirements")
                                                               .get(0)
                                                               .get("conditions")
                                                               .get(0));
        requirement.put("jmespath", JmpesPath);

        return subscriptionJson;
    }
}
