package com.ericsson.ei.utils;

import com.ericsson.ei.MongoClientInitializer;
import com.mongodb.MongoClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestContextInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContextInitializer.class);

    @Getter
    private static MongoClient mongoClient;

    @Override
    public void initialize(ConfigurableApplicationContext ac) {
        try {
            amqpBroker();
            mongoClient = MongoClientInitializer.borrow();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}