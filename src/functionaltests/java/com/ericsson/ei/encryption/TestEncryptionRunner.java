package com.ericsson.ei.encryption;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/encryption.feature", glue = {
        "com.ericsson.ei.encryption" }, plugin = {
                "html:target/cucumber-reports/TestEncryptionRunner" })
public class TestEncryptionRunner {

    @BeforeClass
    public static void configureLdapProperties() {
    }
}