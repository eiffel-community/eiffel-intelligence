package com.ericsson.ei.integrationtests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/SourceChangeFlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests" }, plugin = { "pretty",
                "html:target/cucumber-reports/SourceChangeFlowRunnerIT" })
public class SourceChangeFlowRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregated.collection.name", "aggregated_objects_source_change_flow");
        System.setProperty("waitlist.collection.name", "wait_list_source_change_flow");
        System.setProperty("subscription.collection.name", "subscription_source_change_flow");
        System.setProperty("event_object_map.collection.name", "event_object_map_source_change_flow");
        System.setProperty("subscription.collection.repeatFlagHandlerName", "subscription_repeat_handler_source_change_flow");
        System.setProperty("missedNotificationCollectionName", "missed_notification_source_change_flow");
        System.setProperty("sessions.collection.name", "sessions_source_change_flow");

        System.setProperty("rules.path", "/SourceChangeObjectRules.json");
        System.setProperty("rabbitmq.consumerName", "source_change_queue");
    }
}