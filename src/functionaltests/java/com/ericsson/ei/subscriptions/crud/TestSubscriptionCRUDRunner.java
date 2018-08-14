package com.ericsson.ei.subscriptions.crud;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionCRUD.feature", glue = {
        "com.ericsson.ei.subscriptions.crud"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionCRUDRunner"})
public class TestSubscriptionCRUDRunner extends BaseRunner {

}