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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@PropertySource(value = "file:{security.path}/security.properties", ignoreResourceNotFound = true)

@EnableWebSecurity
public class EndpointSecurity extends WebSecurityConfigurerAdapter {
    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;
    
    @Value("${ldap.url:default}")
    private String ldapUrl;
    
    @Value("${ldap.base.dn:default}")
    private String ldapBaseDn;
    
    @Value("${ldap.username:default}")
    private String ldapUsername;
    
    @Value("${ldap.password:default}")
    private String ldapPassword;
    
    @Value("${ldap.user.filter:default}")
    private String ldapUserFilter;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if(ldapEnabled) {
            http
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .httpBasic()
            .and()
                .csrf().disable();
        }
        else {
            http
            .csrf().disable();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        if(ldapEnabled) {
            auth
            .eraseCredentials(false)
            .ldapAuthentication()
                .userSearchFilter(ldapUserFilter)
                .contextSource()
                    .url(ldapUrl)
                    .root(ldapBaseDn)
                    .managerDn(ldapUsername)
                    .managerPassword(decodeBase64(ldapPassword));
        }
    }
    
    private String decodeBase64(String password) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(password));
    }
}
