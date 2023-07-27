package com.ericsson.ei.rules;


import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rulesHandler.feature", glue = {
        "com.ericsson.ei.rules" }, plugin = { "pretty", "html:target/cucumber-reports/TestRulesHandlerRunner" })
public class RulesHandlerTestRunner {

}