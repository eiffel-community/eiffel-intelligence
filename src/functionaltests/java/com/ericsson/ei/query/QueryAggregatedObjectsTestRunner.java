package com.ericsson.ei.query;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/queryAggregatedObjects.feature", glue = {
        "com.ericsson.ei.query" }, plugin = { "pretty",
                "html:target/cucumber-reports/QueryAggregatedObjectsTestRunner" })
public class QueryAggregatedObjectsTestRunner {

}