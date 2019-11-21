/*
   Copyright 2017 Ericsson AB.
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

package com.ericsson.ei;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.ericsson.ei.exception.AbortExecutionException;
import com.ericsson.ei.utils.EncryptionFormatter;
import com.ericsson.ei.utils.Encryptor;

@Configuration
@EnableWebSecurity
public class EndpointSecurity extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointSecurity.class);

    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;

    @Value("${ldap.server.list:}")
    private String ldapServerList;
    
    @Autowired
    private Encryptor encryptor;;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if(ldapEnabled) {
            LOGGER.info("LDAP security configuration is enabled");
            http
            .authorizeRequests()
                .antMatchers("/auth/*").authenticated()
                .antMatchers(HttpMethod.POST, "/subscriptions").authenticated()
                .antMatchers(HttpMethod.PUT, "/subscriptions").authenticated()
                .antMatchers(HttpMethod.DELETE, "/subscriptions/*").authenticated()
                .anyRequest().permitAll()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                .logout().logoutUrl("/auth/logout").logoutSuccessUrl("/").deleteCookies("SESSION").invalidateHttpSession(true)
            .and()
                .httpBasic()
            .and()
                .csrf().disable();
        }
        else {
            LOGGER.info("LDAP security configuration is disabled");
            http
            .csrf().disable();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (ldapEnabled && !ldapServerList.isEmpty()) {
            JSONArray serverList = new JSONArray(ldapServerList);
            addLDAPServersFromList(serverList, auth);
        }
    }

    private String decodeBase64(String password) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(password));
    }

    private void addLDAPServersFromList(JSONArray serverList, AuthenticationManagerBuilder auth) throws Exception {
 
        for (int i = 0; i < serverList.length(); i++) {
            JSONObject server = (JSONObject) serverList.get(i);
            String password = server.getString("password");
        
            if (EncryptionFormatter.isEncrypted(password)) {
                password = decryptPassword(password);
            }
            else {
                password = decodeBase64(password);
            }
            
            auth
            .eraseCredentials(false)
            .ldapAuthentication()
                .userSearchFilter(server.getString("user.filter"))
                .contextSource()
                    .url(server.getString("url"))
                    .root(server.getString("base.dn"))
                    .managerDn(server.getString("username"))
                    .managerPassword(password);
        }
    }
    
    private String decryptPassword(final String inputEncryptedPassword) throws Exception {
        if (encryptor.isJasyptPasswordSet()) {
            LOGGER.error("Property -jasypt.encryptor.password need to be set for decrypting LDAP password.");
            throw new AbortExecutionException("Failed to initiate LDAP when password is encrypted. " + 
                                "Property -jasypt.encryptor.password need to be set for decrypting LDAP password.");
        }

        String encryptedPassword = EncryptionFormatter.removeEncryptionParentheses(inputEncryptedPassword);
        return encryptor.decrypt(encryptedPassword);
    }
}
