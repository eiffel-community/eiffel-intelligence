package com.ericsson.ei.subscriptions.authentication;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/authentication.feature", glue = {
        "com.ericsson.ei.subscriptions.authentication" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestAuthenticationRunner" })
public class TestAuthenticationRunner {

}