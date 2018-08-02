package com.ericsson.ei;

import com.ericsson.ei.subscriptionhandler.SpringRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import static org.mockito.Mockito.mock;

@Profile("beans")
@Configuration
public class ConfigurateBeans {

    @Bean
    @Primary
    public SpringRestTemplate springRestTemplate() {
        return mock(SpringRestTemplate.class);
    }

    @Bean
    @Primary
    public Authentication authentication() {
        return mock(Authentication.class);
    }

    @Bean
    @Primary
    public SecurityContext securityContext() {
        return mock(SecurityContext.class);
    }
}
