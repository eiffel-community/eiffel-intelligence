package com.ericsson.ei.subscriptions.bulk;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionBulk.feature", glue = {
        "com.ericsson.ei.subscriptions.bulk" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionBulkRunner" })
public class TestSubscriptionBulkRunner {

}