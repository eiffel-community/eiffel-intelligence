package com.ericsson.ei.notifications.ttl;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ttl.feature", glue = {
        "com.ericsson.ei.notifications.ttl"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestTTLRunner"})
public class TestTTLRunner {

}
