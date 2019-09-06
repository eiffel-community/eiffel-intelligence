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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class EndpointSecurity extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointSecurity.class);

    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;

    @Value("${ldap.server.list:}")
    private String ldapServerList;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (ldapEnabled && !ldapServerList.isEmpty()) {
            JSONArray serverList = new JSONArray(ldapServerList);
            addLDAPServersFromList(serverList, auth);
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (ldapEnabled) {
            LOGGER.info("LDAP security configuration is enabled");
            configureRequestAuthorization(http);
            configureSession(http);
            configureLogout(http);
            configureBasicAuth(http);
            disableCSRF(http);
        } else {
            LOGGER.info("LDAP security configuration is disabled");
            disableCSRF(http);
        }
    }

    private void addLDAPServersFromList(JSONArray serverList, AuthenticationManagerBuilder auth)
            throws Exception {
        for (int i = 0; i < serverList.length(); i++) {
            JSONObject server = (JSONObject) serverList.get(i);
            auth.eraseCredentials(false)
                .ldapAuthentication()
                .userSearchFilter(server.getString("user.filter"))
                .contextSource()
                .url(server.getString("url"))
                .root(server.getString("base.dn"))
                .managerDn(server.getString("username"))
                .managerPassword(decodeBase64(server.getString("password")));
        }
    }

    private void configureRequestAuthorization(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/auth/*")
            .authenticated()
            .antMatchers(HttpMethod.POST, "/subscriptions")
            .authenticated()
            .antMatchers(HttpMethod.PUT, "/subscriptions")
            .authenticated()
            .antMatchers(HttpMethod.DELETE, "/subscriptions")
            .authenticated()
            .antMatchers(HttpMethod.DELETE, "/subscriptions/*")
            .authenticated()
            .anyRequest()
            .permitAll();
    }

    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout()
            .logoutUrl("/auth/logout")
            .logoutSuccessUrl("/")
            .deleteCookies("SESSION")
            .invalidateHttpSession(true);
    }

    private void configureBasicAuth(HttpSecurity http) throws Exception {
        http.httpBasic();
    }

    private void disableCSRF(HttpSecurity http) throws Exception {
        http.csrf()
            .disable();
    }

    private String decodeBase64(String password) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(password));
    }
}
