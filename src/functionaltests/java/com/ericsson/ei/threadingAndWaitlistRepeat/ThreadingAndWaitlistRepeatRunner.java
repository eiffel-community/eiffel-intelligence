package com.ericsson.ei.threadingAndWaitlistRepeat;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/threadingAndWaitlistRepeat.feature", glue = {
        "com.ericsson.ei.threadingAndWaitlistRepeat"}, plugin = {
        "html:target/cucumber-reports/ThreadingAndWaitlistRepeatRunner"})
public class ThreadingAndWaitlistRepeatRunner extends BaseRunner {

}