package com.ericsson.ei.subscriptions.content;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionContent.feature", glue = {
        "com.ericsson.ei.subscriptions.content"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionContentRunner"})
public class TestSubscriptionContentRunner {

}
