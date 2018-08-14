package com.ericsson.ei.files;

import com.ericsson.ei.utils.BaseRunner;
import cucumber.api.CucumberOptions;

@CucumberOptions(features = "src/functionaltests/resources/features/downloadFiles.feature", glue = {
        "com.ericsson.ei.files"}, plugin = {"pretty",
        "html:target/cucumber-reports/DownloadFilesTestRunner"})
public class DownloadFilesTestRunner extends BaseRunner {

}