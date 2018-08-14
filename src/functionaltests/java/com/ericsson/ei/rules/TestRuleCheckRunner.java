package com.ericsson.ei.rules;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/ruleCheck.feature", glue = {
        "com.ericsson.ei.rules"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestRuleCheckRunner"})
public class TestRuleCheckRunner extends BaseRunner {

}