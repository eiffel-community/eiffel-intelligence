package com.ericsson.ei.subscriptions.repeatHandler;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionRepeatHandler.feature", glue = {
        "com.ericsson.ei.subscriptions.repeatHandler" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionRepeatHandlerRunner" })
public class TestSubscriptionRepeatHandlerRunner {

}
