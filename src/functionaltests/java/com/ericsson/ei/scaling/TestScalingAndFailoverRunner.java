package com.ericsson.ei.scaling;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/scalingAndFailover.feature", glue = {
        "com.ericsson.ei.scaling" }, plugin = { "html:target/cucumber-reports/TestScalingAndFailoverRunner" })
public class TestScalingAndFailoverRunner {

}