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
package com.ericsson.ei.utils;

import org.junit.ClassRule;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

public class TestLDAPStarter {
    private static final String DOMAIN_DSN = "dc=example,dc=com";
    @ClassRule
    public static EmbeddedLdapRule embeddedLdapRule1 = EmbeddedLdapRuleBuilder.newInstance().usingDomainDsn(DOMAIN_DSN)
    .importingLdifs("ldap-users-first.ldif").build();
    @ClassRule
    public static EmbeddedLdapRule embeddedLdapRule2 = EmbeddedLdapRuleBuilder.newInstance().usingDomainDsn(DOMAIN_DSN)
    .importingLdifs("ldap-users-second.ldif").build();
}
