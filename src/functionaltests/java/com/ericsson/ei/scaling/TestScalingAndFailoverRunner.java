package com.ericsson.ei.scaling;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/scalingAndFailover.feature", glue = {
        "com.ericsson.ei.scaling"}, plugin = {
        "html:target/cucumber-reports/TestSubscriptionTriggerRunner"})
public class TestScalingAndFailoverRunner extends BaseRunner {

}