package com.ericsson.ei.encryption;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/encryption.feature", glue = {
        "com.ericsson.ei.encryption" }, plugin = {
                "html:target/cucumber-reports/TestEncryptionRunner" })
public class TestEncryptionRunner {

    @BeforeClass
    public static void configureLdapProperties() {
    }
}