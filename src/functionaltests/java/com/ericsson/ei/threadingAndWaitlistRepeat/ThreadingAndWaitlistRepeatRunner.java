package com.ericsson.ei.threadingAndWaitlistRepeat;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/threadingAndWaitlistRepeat.feature", glue = {
        "com.ericsson.ei.threadingAndWaitlistRepeat" }, plugin = {
                "html:target/cucumber-reports/ThreadingAndWaitlistRepeatRunner" })
public class ThreadingAndWaitlistRepeatRunner {

}