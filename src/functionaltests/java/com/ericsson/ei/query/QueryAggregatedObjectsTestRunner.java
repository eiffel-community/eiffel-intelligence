package com.ericsson.ei.query;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/queryAggregatedObjects.feature", glue = {
        "com.ericsson.ei.query"}, plugin = {"pretty",
        "html:target/cucumber-reports/QueryAggregatedObjectsTestRunner"})
public class QueryAggregatedObjectsTestRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}