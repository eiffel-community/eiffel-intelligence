package com.ericsson.ei.integrationtests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/FlowIT.feature", glue = {
        "com.ericsson.ei.integrationtests" }, plugin = { "pretty",
                "html:target/cucumber-reports/FlowRunnerIT" })
public class FlowRunnerIT {

}