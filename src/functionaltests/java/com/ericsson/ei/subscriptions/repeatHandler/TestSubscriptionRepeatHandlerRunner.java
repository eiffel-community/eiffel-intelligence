package com.ericsson.ei.subscriptions.repeatHandler;


import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionRepeatHandler.feature", glue = {
        "com.ericsson.ei.subscriptions.repeatHandler" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionRepeatHandlerRunner" })
public class TestSubscriptionRepeatHandlerRunner {

}
