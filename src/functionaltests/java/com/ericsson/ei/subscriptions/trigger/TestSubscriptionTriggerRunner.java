package com.ericsson.ei.subscriptions.trigger;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionTrigger.feature", glue = {
        "com.ericsson.ei.subscriptions.trigger" }, plugin = {
                "html:target/cucumber-reports/TestSubscriptionTriggerRunner" })
public class TestSubscriptionTriggerRunner {

}