package com.ericsson.ei.integrationtests.flow;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/TestExecutionFlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests.flow" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestExecutionFlowRunnerIT" })
public class TestExecutionRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregations.collection.name", "aggregated_objects_test_execution_flow");
        System.setProperty("wait.list.collection.name", "wait_list_test_execution_flow");
        System.setProperty("subscriptions.collection.name", "subscription_test_execution_flow");
        System.setProperty("event.object.map.collection.name", "event_object_map_test_execution_flow");
        System.setProperty("subscriptions.repeat.handler.collection.name", "subscription_repeat_handler_test_execution_flow");
        System.setProperty("failed.notifications.collection.name", "failed_notification_test_execution_change_flow");
        System.setProperty("sessions.collection.name", "sessions_test_execution_flow");

        System.setProperty("rules.path", "/rules/TestExecutionObjectRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.consumer.name", "test_execution_queue");
    }
}