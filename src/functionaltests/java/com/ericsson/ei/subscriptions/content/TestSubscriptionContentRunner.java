package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionContent.feature", glue = {
        "com.ericsson.ei.subscriptions.content"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionContentRunner"})
public class TestSubscriptionContentRunner extends BaseRunner {

}