package com.ericsson.ei.subscriptions.repeatHandler;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionRepeatHandler.feature", glue = {
        "com.ericsson.ei.subscriptions.repeatHandler"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionRepeatHandlerRunner"})
public class TestSubscriptionRepeatHandlerRunner extends BaseRunner {

}