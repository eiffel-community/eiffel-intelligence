package com.ericsson.ei.integrationtests.flow;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/ArtifactFlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests.flow" }, plugin = { "pretty",
                "html:target/cucumber-reports/ArtifactFlowRunnerIT"})
public class ArtifactFlowRunnerIT {
    @BeforeClass
    public static void before() {
        System.setProperty("aggregations.collection.name", "aggregated_objects_artifact_flow");
        System.setProperty("waitlist.collection.name", "wait_list_artifact_flow");
        System.setProperty("subscriptions.collection.name", "subscription_artifact_flow");
        System.setProperty("event.object.map.collection.name", "event_object_map_artifact_flow");
        System.setProperty("subscriptions.repeat.handler.collection.name", "subscription_repeat_handler_artifact_flow");
        System.setProperty("failed.notifications.collection.name", "failed_notification_artifact_flow");
        System.setProperty("sessions.collection.name", "sessions_artifact_flow");

        System.setProperty("rules.path", "/rules/ArtifactRules-Eiffel-Agen-Version.json");
        System.setProperty("rabbitmq.queue.suffix", "artifact_queue");
    }
}