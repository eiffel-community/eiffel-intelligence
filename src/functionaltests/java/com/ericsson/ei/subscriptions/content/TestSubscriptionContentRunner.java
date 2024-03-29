package com.ericsson.ei.subscriptions.content;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionContent.feature", glue = {
        "com.ericsson.ei.subscriptions.content" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestSubscriptionContentRunner" })
public class TestSubscriptionContentRunner {

}