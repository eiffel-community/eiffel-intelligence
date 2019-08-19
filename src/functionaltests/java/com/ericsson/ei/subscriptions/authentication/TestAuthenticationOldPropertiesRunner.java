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