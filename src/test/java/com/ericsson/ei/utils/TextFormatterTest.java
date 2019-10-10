package com.ericsson.ei.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.ericsson.ei.utils.TextFormatter;

public class TextFormatterTest {
    
    @Test
    public void TestRemoveEncryptionParentheses() {
        final String encryptedPassword = "ENC(m0++Ua2Pe1j/5X/x8NpKsQ==)";
        final String expectedEncryptedPassword = "m0++Ua2Pe1j/5X/x8NpKsQ==";
        
        assertEquals("Failed to remove ENC() from encrypted password property string.",
                expectedEncryptedPassword,
                new TextFormatter().removeEncryptionParentheses(encryptedPassword));
    }
}
