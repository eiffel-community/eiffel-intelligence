package com.ericsson.ei.subscriptions.authentication;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.ericsson.ei.utils.TestLDAPStarter;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/functionaltests/resources/features/authentication.feature",
        "src/functionaltests/resources/features/authenticationMultiLDAP.feature" }, glue = {
                "com.ericsson.ei.subscriptions.authentication" }, plugin = { "pretty",
                        "html:target/cucumber-reports/TestAuthenticationRunner" })
public class TestAuthenticationRunner extends TestLDAPStarter {

    @BeforeClass
    public static void configureLdapProperties() {
        int ldapPort1 = TestLDAPStarter.embeddedLdapRule1.embeddedServerPort();
        int ldapPort2 = TestLDAPStarter.embeddedLdapRule2.embeddedServerPort();
        System.setProperty("ldap.server.list", "[{" +
                "\"url\":\"ldap://localhost:" + ldapPort1 + "/dc=example,dc=com\"," +
                "\"base.dn\":\"\"," +
                "\"username\":\"\"," +
                "\"password\":\"\"," +
                "\"user.filter\":\"uid={0}\"" +
                "}," +
                "{" +
                "\"url\":\"ldap://localhost:" + ldapPort2 + "/dc=example,dc=com\"," +
                "\"base.dn\":\"\"," +
                "\"username\":\"\"," +
                "\"password\":\"\"," +
                "\"user.filter\":\"uid={0}\"" +
                "}]");
    }
}