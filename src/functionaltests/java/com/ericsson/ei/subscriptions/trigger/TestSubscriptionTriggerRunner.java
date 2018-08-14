package com.ericsson.ei.subscriptions.trigger;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionTrigger.feature", glue = {
        "com.ericsson.ei.subscriptions.trigger"}, plugin = {
        "html:target/cucumber-reports/TestSubscriptionTriggerRunner"})
public class TestSubscriptionTriggerRunner extends BaseRunner {

}