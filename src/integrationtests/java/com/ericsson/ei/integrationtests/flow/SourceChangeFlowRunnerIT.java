package com.ericsson.ei.integrationtests.flow;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/SourceChangeFlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests.flow" }, plugin = { "pretty",
                "html:target/cucumber-reports/SourceChangeFlowRunnerIT" })
public class SourceChangeFlowRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregations.collection.name", "aggregated_objects_source_change_flow");
        System.setProperty("wait.list.collection.name", "wait_list_source_change_flow");
        System.setProperty("subscriptions.collection.name", "subscription_source_change_flow");
        System.setProperty("event.object.map.collection.name", "event_object_map_source_change_flow");
        System.setProperty("subscriptions.repeat.handler.collection.name", "subscription_repeat_handler_source_change_flow");
        System.setProperty("failed.notifications.collection.name", "failed_notification_source_change_flow");
        System.setProperty("sessions.collection.name", "sessions_source_change_flow");

        System.setProperty("rules.path", "/rules/SourceChangeObjectRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.consumer.name", "source_change_queue");
    }
}