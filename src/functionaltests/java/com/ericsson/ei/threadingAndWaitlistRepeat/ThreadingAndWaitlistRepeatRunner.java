package com.ericsson.ei.threadingAndWaitlistRepeat;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/threadingAndWaitlistRepeat.feature", glue = {
        "com.ericsson.ei.threadingAndWaitlistRepeat"}, plugin = {
        "html:target/cucumber-reports/ThreadingAndWaitlistRepeatRunner"})
public class ThreadingAndWaitlistRepeatRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}