package com.ericsson.ei.subscriptions.crud;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionCRUD.feature", glue = {
        "com.ericsson.ei.subscriptions.crud" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionCRUDRunner" })
public class TestSubscriptionCRUDRunner {

}