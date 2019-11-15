package com.ericsson.ei.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class SafeLdapServer {

    /**
     * By creating our own LDAP setting object to display we have control of what values are
     * shown to the user. This also makes it impossible to slip unwanted values by mistake if
     * the user configure the property with misspelled keys.
     *
     * @param String :
     * @return JSONArray
     */
    public static JSONArray createLdapSettingsArray(String inputServerList) {
        if (StringUtils.isBlank(inputServerList)) {
            return new JSONArray();
        }
        JSONArray modifiedServerList = new JSONArray();

        final JSONArray serverList = new JSONArray(inputServerList);
        serverList.forEach(item -> {
            JSONObject ldapServer = (JSONObject) item;
            JSONObject modifiedLdapServer = extractLdapValues(ldapServer);
            modifiedServerList.put(modifiedLdapServer);
        });
        return modifiedServerList;
    }

    /**
     * Extracts the specified values from the input JSONObject to the returned JSONObject.
     *
     * @param JSONObject
     * @return JSONObject
     */
    private static JSONObject extractLdapValues(JSONObject ldapServer) {
        JSONObject modifiedLdapServer = new JSONObject();
        modifiedLdapServer.put("user.filter", ldapServer.get("user.filter"));
        modifiedLdapServer.put("base.dn", ldapServer.get("base.dn"));
        modifiedLdapServer.put("username", ldapServer.get("username"));
        modifiedLdapServer.put("url", ldapServer.get("url"));
        return modifiedLdapServer;
    }
}
