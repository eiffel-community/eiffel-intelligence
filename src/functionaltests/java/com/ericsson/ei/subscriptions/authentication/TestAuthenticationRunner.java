package com.ericsson.ei.subscriptions.authentication;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import com.ericsson.ei.utils.TestLDAPStarter;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/authentication.feature", glue = {
        "com.ericsson.ei.subscriptions.authentication" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestAuthenticationRunner" })
public class TestAuthenticationRunner extends TestLDAPStarter {
}