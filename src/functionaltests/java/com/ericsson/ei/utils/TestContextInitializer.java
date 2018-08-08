package com.ericsson.ei.utils;

import com.mongodb.MongoClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.SocketUtils;

import java.io.IOException;

public class TestContextInitializer extends TestConfigs
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContextInitializer.class);

    public static MongoClient mongoClient;

    @Override
    public void initialize(ConfigurableApplicationContext ac) {

        try {
            createAmqpBroker(); // get instance from pool where? here, or from tests steps?
            mongoClient = startUpMongoClient();
        } catch (Exception e) {
            LOGGER.error("Failed to startup Mongo client or AMQP broker for test");
        }
    }
}