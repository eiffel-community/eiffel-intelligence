package com.ericsson.ei.subscriptions.crud;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/subscriptionCRUD.feature", glue = {
        "com.ericsson.ei.subscriptions.crud"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestSubscriptionCRUDRunner"})
public class TestSubscriptionCRUDRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}