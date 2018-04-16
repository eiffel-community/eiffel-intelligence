package com.ericsson.ei.config;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@PropertySource(value = "file:${catalina.home}/conf/security.properties", ignoreResourceNotFound = true)
@ConditionalOnProperty(value = "ldap.enabled")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${ldap.url}")
    private String ldapUrl;
    
    @Value("${ldap.base.dn}")
    private String ldapBaseDn;
    
    @Value("${ldap.username}")
    private String ldapUsername;
    
    @Value("${ldap.password}")
    private String ldapPassword;
    
    @Value("${ldap.user.filter}")
    private String ldapUserFilter;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .and()
                .httpBasic()
            .and()
                .csrf().disable();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
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
    
    private String decodeBase64(String password) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(password));
    }
}
