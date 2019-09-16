package com.ericsson.ei.integrationtests.notification;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/FailedNotificationIT.feature", glue = {
        "com.ericsson.ei.integrationtests.notification" }, plugin = { "pretty",
                "html:target/cucumber-reports/FailedNotificationRunnerIT" })
public class FailedNotificationRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregated.collection.name", "aggregated_objects_artifact_flow");
        System.setProperty("waitlist.collection.name", "wait_list_artifact_flow");
        System.setProperty("subscription.collection.name", "subscription_artifact_flow");
        System.setProperty("event_object_map.collection.name", "event_object_map_artifact_flow");
        System.setProperty("subscription.collection.repeatFlagHandlerName",
                "subscription_repeat_handler_artifact_flow");
        System.setProperty("missedNotificationCollectionName", "missed_notification_artifact_flow");
        System.setProperty("sessions.collection.name", "sessions_artifact_flow");

        System.setProperty("rules.path", "/rules/ArtifactRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.consumerName", "artifact_queue");
    }
}
