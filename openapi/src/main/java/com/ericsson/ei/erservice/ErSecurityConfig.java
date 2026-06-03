package com.ericsson.ei.erservice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "er.security.permitAll", havingValue = "true")
public class ErSecurityConfig {
    @Bean
    public SecurityFilterChain erFilterChain(HttpSecurity http) throws Exception {
        // CSRF disabled intentionally: this is a test-only mock ER service, not production code
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
