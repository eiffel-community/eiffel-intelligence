package com.ericsson.ei.threadingAndWaitlistRepeat;

import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/threadingAndWaitlistRepeat.feature", glue = {
        "com.ericsson.ei.threadingAndWaitlistRepeat" }, plugin = {
                "html:target/cucumber-reports/ThreadingAndWaitlistRepeatRunner" })
public class ThreadingAndWaitlistRepeatRunner {

}