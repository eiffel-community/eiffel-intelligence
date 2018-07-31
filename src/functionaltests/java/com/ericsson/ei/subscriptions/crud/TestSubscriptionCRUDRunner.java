package com.ericsson.ei.subscriptions.crud;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionCRUD.feature", glue = {
        "com.ericsson.ei.subscriptions.crud"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionCRUDRunner"})
public class TestSubscriptionCRUDRunner {

}
