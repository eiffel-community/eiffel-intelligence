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
     * Function handle also "ENC(<encrypted password>" with missing end , ')', parentheses.
     * 
     * @param stringWithEncryptionParantheses  The string that contains the ENC() string.
     * 
     * @return  Then string formatted encrypted password without ENC().
     */
    public String removeEncryptionParentheses(String stringWithEncryptionParantheses) {
        String formattedEncryptionString = stringWithEncryptionParantheses.replace("ENC(","");
        int lastParanthesesOccurenxeIndex = formattedEncryptionString.lastIndexOf(")");
        if (lastParanthesesOccurenxeIndex == -1) {
            return formattedEncryptionString;
        }
        formattedEncryptionString = formattedEncryptionString.subSequence(0, lastParanthesesOccurenxeIndex).toString();
        return formattedEncryptionString;
    }
}
