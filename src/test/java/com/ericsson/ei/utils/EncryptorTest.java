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

import org.junit.Test;

public class EncryptorTest {

    @Test
    public void testRemoveEncryptionParentheses() {
        final String encryptedPassword = "ENC(m0++Ua2Pe1j/5X/x8NpKsQ==)";
        final String expectedEncryptedPassword = "m0++Ua2Pe1j/5X/x8NpKsQ==";

        assertEquals("Failed to remove ENC() from encrypted password property string.",
                expectedEncryptedPassword,
                EncryptionFormatter.removeEncryptionParentheses(encryptedPassword));
    }

    @Test
    public void testRemoveEncryptionParenthesesWithMissingEndParentheses() {
        final String encryptedPassword = "ENC(m0++Ua2Pe1j/5X/x8NpKsQ==";
        final String expectedEncryptedPassword = "m0++Ua2Pe1j/5X/x8NpKsQ==";

        assertEquals("Failed to remove ENC() from encrypted password property string.",
                expectedEncryptedPassword,
                EncryptionFormatter.removeEncryptionParentheses(encryptedPassword));
    }
}
