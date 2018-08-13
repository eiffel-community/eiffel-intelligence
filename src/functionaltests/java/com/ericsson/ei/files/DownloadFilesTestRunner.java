package com.ericsson.ei.files;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/downloadFiles.feature", glue = {
        "com.ericsson.ei.files" }, plugin = { "pretty", "html:target/cucumber-reports/DownloadFilesTestRunner" })
public class DownloadFilesTestRunner {

}