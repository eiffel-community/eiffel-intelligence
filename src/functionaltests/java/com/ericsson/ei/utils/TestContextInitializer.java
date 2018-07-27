package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestContextInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext ac) {
        try {
            amqpBroker();
            mongoClient();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}