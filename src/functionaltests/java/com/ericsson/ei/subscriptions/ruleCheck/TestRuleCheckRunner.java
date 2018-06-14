package com.ericsson.ei.subscriptions.ruleCheck;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ruleCheck.feature", glue = {
        "com.ericsson.ei.subscriptions.ruleCheck" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestRuleCheckRunner" }, monochrome = false)
public class TestRuleCheckRunner {

}
