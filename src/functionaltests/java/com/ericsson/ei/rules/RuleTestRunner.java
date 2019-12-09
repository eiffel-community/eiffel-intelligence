package com.ericsson.ei.rules;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ruleTest.feature", glue = {
        "com.ericsson.ei.rules" }, plugin = { "pretty", "html:target/cucumber-reports/RuleTestRunner" })
public class RuleTestRunner {

}