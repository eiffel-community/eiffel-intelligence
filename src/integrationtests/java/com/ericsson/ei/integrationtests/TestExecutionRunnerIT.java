package com.ericsson.ei.integrationtests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/TestExecutionFlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestExecutionFlowRunnerIT" })
public class TestExecutionRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregated.collection.name", "aggregated_objects_test_execution_flow");
        System.setProperty("waitlist.collection.name", "wait_list_test_execution_flow");
        System.setProperty("subscription.collection.name", "subscription_test_execution_flow");
        System.setProperty("event_object_map.collection.name", "event_object_map_test_execution_flow");
        System.setProperty("subscription.collection.repeatFlagHandlerName", "subscription_repeat_handler_test_execution_flow");
        System.setProperty("missedNotificationCollectionName", "missed_notification_test_execution_change_flow");
        System.setProperty("sessions.collection.name", "sessions_test_execution_flow");

        System.setProperty("rules.path", "/TestExecutionObjectRules.json");
        System.setProperty("rabbitmq.consumerName", "test_execution_queue");
    }
}