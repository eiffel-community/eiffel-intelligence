package com.ericsson.ei.utils;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestContextInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ac) {
        try {
            amqpBroker();
            mongoClient();
            // setUpEmbeddedMongo();
            int i = 0;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}