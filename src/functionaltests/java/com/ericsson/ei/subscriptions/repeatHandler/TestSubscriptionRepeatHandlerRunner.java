package com.ericsson.ei.subscriptions.repeatHandler;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionRepeatHandler.feature", glue = {
        "com.ericsson.ei.subscriptions.repeatHandler"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionRepeatHandleRunner"})
public class TestSubscriptionRepeatHandlerRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}