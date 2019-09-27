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
        System.setProperty("aggregated.collection.name", "aggregated_failed_notification");
        System.setProperty("waitlist.collection.name", "wait_list_failed_notification");
        System.setProperty("subscription.collection.name", "subscription_failed_notification");
        System.setProperty("event_object_map.collection.name", "event_object_map_failed_notification");
        System.setProperty("subscription.collection.repeatFlagHandlerName",
                "subscription_repeat_handler_failed_notification");
        System.setProperty("missedNotificationCollectionName", "missed_notification_failed_notification");
        System.setProperty("sessions.collection.name", "sessions_failed_notification");

        System.setProperty("rules.path", "/rules/ArtifactRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.consumerName", "failed_notification_queue");
    }
}
