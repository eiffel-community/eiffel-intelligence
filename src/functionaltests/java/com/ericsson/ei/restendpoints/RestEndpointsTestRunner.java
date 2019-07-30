package com.ericsson.ei.restendpoints;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/restEndpoints.feature", glue = {
        "com.ericsson.ei.restendpoints" }, plugin = { "pretty",
                "html:target/cucumber-reports/RestEndpointsTestRunner" })
public class RestEndpointsTestRunner {

}