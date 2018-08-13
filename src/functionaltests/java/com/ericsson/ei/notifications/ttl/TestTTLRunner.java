package com.ericsson.ei.notifications.ttl;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ttl.feature", glue = {
        "com.ericsson.ei.notifications.ttl"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestTTLRunner"})
public class TestTTLRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}