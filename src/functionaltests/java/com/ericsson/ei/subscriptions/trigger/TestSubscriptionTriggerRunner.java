package com.ericsson.ei.subscriptions.trigger;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionTrigger.feature", glue = {
        "com.ericsson.ei.subscriptions.trigger"}, plugin = {
        "html:target/cucumber-reports/TestSubscriptionTriggerRunner"})
public class TestSubscriptionTriggerRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}