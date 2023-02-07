package com.ericsson.ei.templates;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/templates.feature", glue = {
        "com.ericsson.ei.templates" }, plugin = { "pretty", "html:target/cucumber-reports/TemplatesTestRunner" })
public class TemplatesTestRunner {

}