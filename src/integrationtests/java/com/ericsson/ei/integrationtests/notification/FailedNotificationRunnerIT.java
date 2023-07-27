package com.ericsson.ei.integrationtests.notification;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/FailedNotificationIT.feature", glue = {
        "com.ericsson.ei.integrationtests.notification" }, plugin = { "pretty",
                "html:target/cucumber-reports/FailedNotificationRunnerIT" })
public class FailedNotificationRunnerIT {
    public static final String FAILED_NOTIFICATION_COLLECTION = "failed_notification";

    @BeforeClass
    public static void before() {
        System.setProperty("aggregations.collection.name", "aggregated_failed_notification");
        System.setProperty("waitlist.collection.name", "wait_list_failed_notification");
        System.setProperty("subscriptions.collection.name", "subscription_failed_notification");
        System.setProperty("event.object.map.collection.name",
                "event_object_map_failed_notification");
        System.setProperty("subscriptions.repeat.handler.collection.name",
                "subscription_repeat_handler_failed_notification");
        System.setProperty("failed.notifications.collection.name", FAILED_NOTIFICATION_COLLECTION);
        System.setProperty("sessions.collection.name", "sessions_failed_notification");
        System.setProperty("rules.path", "/rules/ArtifactRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.queue.suffix", "failed_notification_queue");
    }
}
