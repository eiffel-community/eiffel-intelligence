package com.ericsson.ei.templates;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/templates.feature", glue = {
        "com.ericsson.ei.templates" }, plugin = { "pretty", "html:target/cucumber-reports/TemplatesTestRunner" })
public class TemplatesTestRunner {

}