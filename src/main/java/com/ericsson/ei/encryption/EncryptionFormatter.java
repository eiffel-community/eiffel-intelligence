package com.ericsson.ei.encryption;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.ei.controller.model.AuthenticationType;

public class EncryptionFormatter {
    private static final String ENCRYPTION_WRAPPER = "ENC(%s)";
    private static final String ENCRYPTION_PATTERN = "[eE][nN][cC]\\(([^\\)]*)";

    /**
     * Function that removes ENC parentheses from encrypted string. Commonly used for password
     * properties that has a format "ENC(d23d2ferwf4t55)" This function matches the "ENC(password)"
     * pattern and returns the password string.
     * 
     * Function handle also "ENC(password" with missing ')' parentheses.
     * 
     * @param encryptedString The string that contains the ENC() string.
     * 
     * @return the password string
     */
    public static String removeEncryptionParentheses(String encryptedString) {
        Pattern pattern = Pattern.compile(ENCRYPTION_PATTERN);
        Matcher matcher = pattern.matcher(encryptedString);
        if (matcher.lookingAt()) {
            return matcher.group(1);
        }
        return encryptedString;
    }

    /**
     * Wrap the string around the ENC(password) pattern.
     *
     * @param encryptedString
     * @return the wrapped password string
     */
    public static String addEncryptionParentheses(String encryptedString) {
        return String.format(ENCRYPTION_WRAPPER, encryptedString);
    }

    /**
     * Checks if the provided password string contains the encryption wrapper. The encryption
     * wrapper should look like this: ENC(password)
     *
     * @param password
     * @return true if password is tagged as encryped, false otherwise
     */
    public static boolean isEncrypted(final String password) {
        Pattern pattern = Pattern.compile(ENCRYPTION_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.lookingAt();
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
