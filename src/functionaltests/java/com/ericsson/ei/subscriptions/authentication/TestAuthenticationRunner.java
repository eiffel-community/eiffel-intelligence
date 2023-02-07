/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.subscriptions.authentication;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.ericsson.ei.utils.TestLDAPStarter;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

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