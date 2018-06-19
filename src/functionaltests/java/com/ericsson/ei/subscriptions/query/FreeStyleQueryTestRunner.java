package com.ericsson.ei.subscriptions.query;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/freeStyleQueryTest.feature", glue = {
        "com.ericsson.ei.subscriptions.query" }, plugin = { "pretty",
                "html:target/cucumber-reports/FreeStyleQueryTestRunner" }, monochrome = false)
public class FreeStyleQueryTestRunner {

}
