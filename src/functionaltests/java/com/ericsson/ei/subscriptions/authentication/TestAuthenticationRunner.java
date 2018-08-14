package com.ericsson.ei.subscriptions.authentication;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/authentication.feature", glue = {
        "com.ericsson.ei.subscriptions.authentication"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestAuthenticationRunner"})
public class TestAuthenticationRunner extends BaseRunner {

}