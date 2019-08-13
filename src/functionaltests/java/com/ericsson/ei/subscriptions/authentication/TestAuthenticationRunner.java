package com.ericsson.ei.subscriptions.authentication;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/authentication.feature", glue = {
        "com.ericsson.ei.subscriptions.authentication" }, plugin = { "pretty",
                "html:target/cucumber-reports/TestAuthenticationRunner" })
public class TestAuthenticationRunner {
    private static final String DOMAIN_DSN = "dc=example,dc=com";
    @ClassRule
    public static EmbeddedLdapRule embeddedLdapRule1 = EmbeddedLdapRuleBuilder.newInstance().usingDomainDsn(DOMAIN_DSN)
    .importingLdifs("ldap-users-first.ldif").build();
    @ClassRule
    public static EmbeddedLdapRule embeddedLdapRule2 = EmbeddedLdapRuleBuilder.newInstance().usingDomainDsn(DOMAIN_DSN)
    .importingLdifs("ldap-users-second.ldif").build();
}