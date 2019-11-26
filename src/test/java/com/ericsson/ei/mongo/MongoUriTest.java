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
package com.ericsson.ei.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ericsson.ei.mongo.MongoUri;
import com.mongodb.MongoConfigurationException;

public class MongoUriTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "my_password";
    private static final String NEW_PASSWORD = "my_new_password";
    private static final String URI = String.format(
            "mongodb://%s:%s@hostname1:27017,hostname2:27017", USERNAME, PASSWORD);

    @Test
    public void testGetSafeUri() throws Exception {
        String safeUri = MongoUri.getUriWithHiddenPassword(URI);
        assertFalse("safeUri should not contain password", safeUri.contains(PASSWORD));
    }

    @Test
    public void testGetSafeUriNoUsernameAndPassword() throws Exception {
        String uriNoUserPass = "mongodb://hostname1:27017,hostname2:27017";
        String safeUri = MongoUri.getUriWithHiddenPassword(uriNoUserPass);
        assertEquals("Uri should be unchanged when no username or password is given", uriNoUserPass, safeUri);
    }

    @Test
    public void testGetUriWithUpdatedPassword() throws Exception {
        String updatedUri = MongoUri.getUriWithNewPassword(URI, NEW_PASSWORD);
        assertFalse("safeUri should not contain old password", updatedUri.contains(PASSWORD));
        assertTrue("safeUri should contain new password", updatedUri.contains(NEW_PASSWORD));
        assertTrue("safeUri should still contain username", updatedUri.contains(USERNAME));
    }

    @Test
    public void testGetUriWithUpdatedPasswordSameUsernameAndPassword() throws Exception {
        /**
         * If a uri is mongodb://admin:admin@hostname1:27017 only the password should be changed.
         */
        String uriWithSameUserAndPassword = String.format(
                "mongodb://%s:%s@hostname1:27017,hostname2:27017", USERNAME, USERNAME);
        String expectedNewUri = String.format(
                "mongodb://%s:%s@hostname1:27017,hostname2:27017", USERNAME, NEW_PASSWORD);

        String updatedUri = MongoUri.getUriWithNewPassword(uriWithSameUserAndPassword,
                NEW_PASSWORD);
        assertEquals("Updated uri with same user and password", expectedNewUri, updatedUri);
    }

    @Test
    public void testGetSecret() throws Exception {
        String password = MongoUri.extractPasswordFromUri(URI);
        assertEquals("Extracted correct secret", PASSWORD, password);
    }

    @Test(expected = MongoConfigurationException.class)
    public void exceptionThrownWhenNoUri() throws Exception {
        MongoUri.getUriWithHiddenPassword("");
    }

}
