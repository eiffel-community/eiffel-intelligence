package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.ericsson.ei.subscriptions.authentication.TestAuthenticationRunner;
import com.unboundid.ldap.sdk.LDAPException;

public class TestLDAPInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLDAPInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext ac) {
        try {
            setAuthorization();
            startLdap();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void startLdap() throws LDAPException {
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