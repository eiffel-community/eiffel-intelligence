package com.ericsson.ei.notifications.ttl;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/ttl.feature", glue = {
        "com.ericsson.ei.notifications.ttl"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestTTLRunner"})
public class TestTTLRunner extends BaseRunner {

}