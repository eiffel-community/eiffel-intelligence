package com.ericsson.ei.subscriptions.bulk;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionBulk.feature", glue = {
        "com.ericsson.ei.subscriptions.bulk" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionBulkRunner" })
public class TestSubscriptionBulkRunner {

}