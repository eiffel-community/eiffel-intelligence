package com.ericsson.ei.subscriptions.bulk;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionBulk.feature", glue = {
        "com.ericsson.ei.subscriptions.bulk"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionBulkRunner"})
public class TestSubscriptionBulkRunner extends BaseRunner {

}