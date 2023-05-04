package com.ericsson.ei.restendpoints;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/restEndpoints.feature", glue = {
        "com.ericsson.ei.restendpoints" }, plugin = { "pretty",
                "html:target/cucumber-reports/RestEndpointsTestRunner" })
public class RestEndpointsTestRunner {

}