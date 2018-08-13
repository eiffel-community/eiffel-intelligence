package com.ericsson.ei.rules;

import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/ruleCheck.feature", glue = {
        "com.ericsson.ei.rules"}, plugin = {"pretty",
        "html:target/cucumber-reports/TestRuleCheckRunner"})
public class TestRuleCheckRunner {

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(TestContextInitializer.getMongoClient());
    }
}