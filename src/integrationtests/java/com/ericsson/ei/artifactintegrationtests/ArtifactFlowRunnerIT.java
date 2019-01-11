package com.ericsson.ei.artifactintegrationtests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/integrationtests/resources/features/ArtifactFlowIT.feature", glue = {
        "com.ericsson.ei.artifactintegrationtests" }, plugin = { "pretty",
                "html:target/cucumber-reports/ArtifactFlowRunnerIT" })
public class ArtifactFlowRunnerIT {

}