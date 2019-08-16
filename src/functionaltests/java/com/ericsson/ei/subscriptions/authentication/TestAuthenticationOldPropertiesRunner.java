package com.ericsson.ei.subscriptions.authentication;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.ericsson.ei.utils.TestLDAPStarter;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/authentication.feature", glue = {
                "com.ericsson.ei.subscriptions.authentication" }, plugin = { "pretty",
                        "html:target/cucumber-reports/TestAuthenticationOldPropertiesRunner" })
public class TestAuthenticationOldPropertiesRunner extends TestLDAPStarter {

    @BeforeClass
    public static void configureLdapProperties() {
        int ldapPort1 = TestLDAPStarter.embeddedLdapRule1.embeddedServerPort();
        System.setProperty("ldap.url", "ldap://localhost:" + ldapPort1 + "/dc=example,dc=com");
        System.setProperty("ldap.base.dn", "");
        System.setProperty("ldap.username", "");
        System.setProperty("ldap.password", "");
        System.setProperty("ldap.user.filter", "uid={0}");
        System.setProperty("ldap.server.list", "");
    }
}