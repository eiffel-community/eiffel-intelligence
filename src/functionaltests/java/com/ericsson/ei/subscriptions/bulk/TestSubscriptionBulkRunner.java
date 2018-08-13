package com.ericsson.ei.subscriptions.bulk;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionBulk.feature", glue = {
        "com.ericsson.ei.subscriptions.bulk"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionBulkRunner"})
public class TestSubscriptionBulkRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}