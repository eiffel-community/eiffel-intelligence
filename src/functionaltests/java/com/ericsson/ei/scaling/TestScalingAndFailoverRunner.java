package com.ericsson.ei.scaling;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/scalingAndFailover.feature", glue = {
        "com.ericsson.ei.scaling" }, plugin = { "html:target/cucumber-reports/TestScalingAndFailoverRunner" })
public class TestScalingAndFailoverRunner {

}