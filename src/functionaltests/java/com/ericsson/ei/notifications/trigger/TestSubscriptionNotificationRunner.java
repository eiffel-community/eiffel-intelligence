package com.ericsson.ei.notifications.trigger;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionNotification.feature", glue = {
        "com.ericsson.ei.notifications.trigger" }, plugin = {
                "html:target/cucumber-reports/TestSubscriptionNotificationRunner" })
public class TestSubscriptionNotificationRunner {

}