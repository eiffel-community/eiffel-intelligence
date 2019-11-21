package com.ericsson.ei.utils;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.ei.controller.model.AuthenticationType;

public class EncryptionFormatter {
    /**
     * Function that removes ENC parentheses from encrypted string. Commonly used for password
     * properties that has a format "ENC(d23d2ferwf4t55)" This function removes "ENC(" text and end
     * ")" parentheses and return only the encryption as string.
     * 
     * Function handle also "ENC(<encrypted password>" with missing end , ')', parentheses.
     * 
     * @param stringWithEncryptionParantheses The string that contains the ENC() string.
     * 
     * @return Then string formatted encrypted password without ENC().
     */
    public static String removeEncryptionParentheses(String stringWithEncryptionParantheses) {
        String formattedEncryptionString = stringWithEncryptionParantheses.replace("ENC(", "");
        int lastParanthesesOccurenxeIndex = formattedEncryptionString.lastIndexOf(")");
        if (lastParanthesesOccurenxeIndex == -1) {
            return formattedEncryptionString;
        }
        formattedEncryptionString = formattedEncryptionString.subSequence(0,
                lastParanthesesOccurenxeIndex).toString();
        return formattedEncryptionString;
    }

    /**
     * Checks if the provided password string contains the encryption wrapper. The encryption
     * wrapper should look like this: ENC(myEncryptedPassword)
     *
     * @param password
     * @return true if password is tagged as encryped, false otherwise
     */
    public static boolean isEncrypted(final String password) {
        return (password.startsWith("ENC(") && password.endsWith(")"));
    }

    /**
     * Returns a boolean indicating that authentication details are provided.
     *
     * @param authType
     * @param username
     * @param password
     * @return
     */
    public static boolean verifyAuthenticationDetails(String authType, String username,
            String password) {
        if (StringUtils.isEmpty(authType)
                || authType.equals(AuthenticationType.NO_AUTH.getValue())) {
            return false;
        }

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return false;
        }

        return true;
    }
}
