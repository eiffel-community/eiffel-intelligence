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

import org.junit.Test;

public class MongoUriTest {

    private static final String PASSWORD = "my_password";
    private static final String URI = String.format(
            "mongodb://username:%s@hostname1:27017,hostname2:27017", PASSWORD);

    @Test
    public void testGetSafeUri() throws Exception {
        String safeUri = MongoUri.getSafeUri(URI);
        assertFalse("safeUri should not contain password", safeUri.contains(PASSWORD));
    }

    @Test
    public void testGetSecret() throws Exception {
        String secret = MongoUri.extractSecretFromUri(URI);
        assertEquals("Extracted correct secret", PASSWORD, secret);
    }

}
