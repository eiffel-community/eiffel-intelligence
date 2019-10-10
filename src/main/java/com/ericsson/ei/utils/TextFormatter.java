package com.ericsson.ei.utils;


/**
 * Class that handles and formatting Strings for different needs
 * and can be used in different classes.
 *
 */
public class TextFormatter {
    
    /**
     * Function that removes ENC parentheses from encrypted string.
     * Commonly used for password properties that has a format "ENC(d23d2ferwf4t55)"
     * This function removes "ENC(" text and end ")" parentheses and return only the
     * encryption as string.
     * 
     * @param stringWithEncryptionParantheses  The string that contains the ENC() string.
     */
    public String removeEncryptionParentheses(String stringWithEncryptionParantheses) {
        String formattedEncryptionString = stringWithEncryptionParantheses.replace("ENC(","");
        int lastParanthesesOccurenxeIndex = formattedEncryptionString.lastIndexOf(")");
        formattedEncryptionString = formattedEncryptionString.subSequence(0, lastParanthesesOccurenxeIndex).toString();
        return formattedEncryptionString;
    }
}
