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
