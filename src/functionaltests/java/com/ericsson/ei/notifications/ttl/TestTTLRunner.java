package com.ericsson.ei.notifications.ttl;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ttl.feature", glue = {
        "com.ericsson.ei.notifications.ttl" }, plugin = { "pretty",
        "html:target/cucumber-reports/TestTTLRunner" })
public class TestTTLRunner {

}