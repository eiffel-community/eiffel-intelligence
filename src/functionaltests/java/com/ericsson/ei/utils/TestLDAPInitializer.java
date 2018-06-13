package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestLDAPInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLDAPInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext ac) {
        try {
            setAuthorization();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}