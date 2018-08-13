package com.ericsson.ei.subscriptions.content;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionContent.feature", glue = {
        "com.ericsson.ei.subscriptions.content"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionContentRunner"})
public class TestSubscriptionContentRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}
