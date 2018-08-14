package com.ericsson.ei.query;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/queryAggregatedObjects.feature", glue = {
        "com.ericsson.ei.query"}, plugin = {"pretty",
        "html:target/cucumber-reports/QueryAggregatedObjectsTestRunner"})
public class QueryAggregatedObjectsTestRunner extends BaseRunner {

}