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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SafeLdapServerTest {

    private static final String PASSWORD_KEY = "password";
    private static final String URL_KEY = "url";
    private static final String URL_VALUE = "my-url";

    private static final String SINGLE_LDAP_SERVER = "[{"
            + " \"url\": \"" + URL_VALUE + "\","
            + " \"base.dn\": \"\","
            + " \"username\": \"\","
            + " \"password\": \"\","
            + " \"user.filter\": \"\""
            + " }]";
    private static final String MULTIPLE_LDAP_SERVER =  "[{"
            + " \"url\": \"" + URL_VALUE + "\","
            + " \"base.dn\": \"\","
            + " \"username\": \"\","
            + " \"password\": \"\","
            + " \"user.filter\": \"\""
            + " },"
            + "{"
            + " \"url\": \"" + URL_VALUE + "\","
            + " \"base.dn\": \"\","
            + " \"username\": \"\","
            + " \"password\": \"\","
            + " \"user.filter\": \"\""
            + " }]";

    @Test
    public void testSingleLdapServer() throws Exception {
        final JSONArray serverList = SafeLdapServer.createLdapSettingsArray(SINGLE_LDAP_SERVER);
        assertSafeLdapServerList(serverList);
    }

    @Test
    public void testMultipleLdapServer() throws Exception {
        final JSONArray serverList = SafeLdapServer.createLdapSettingsArray(MULTIPLE_LDAP_SERVER);
        assertSafeLdapServerList(serverList);
    }

    private void assertSafeLdapServerList(JSONArray serverList) {
        serverList.forEach(item -> {
            JSONObject ldapServer = (JSONObject) item;
            Set<String> keys = ldapServer.keySet();
            assertFalse("Safe LDAP object should not contain key password",
                    keys.contains(PASSWORD_KEY));
            assertTrue("Safe LDAP object should contain key url", keys.contains(URL_KEY));
            assertEquals("Safe LDAP object url value", URL_VALUE, ldapServer.get(URL_KEY));
        });
    }

    @Test
    public void testInputValueNull() throws Exception {
        final JSONArray serverList = SafeLdapServer.createLdapSettingsArray("");
        assertEquals("Safe LDAP server list should be empty", 0, serverList.length());
    }

    @Test
    public void testInputValueEmpty() throws Exception {
        final JSONArray serverList = SafeLdapServer.createLdapSettingsArray("");
        assertEquals("Safe LDAP server list should be empty", 0, serverList.length());
    }

}
