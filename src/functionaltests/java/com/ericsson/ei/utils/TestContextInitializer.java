package com.ericsson.ei.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.ericsson.ei.test.utils.TestConfigs;

public class TestContextInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext ac) {

        setRules();

        try {
            init();
        } catch (Exception e) {
            LOGGER.error("Failed to startup Mongo client or AMQP broker for test");
        }
    }
}