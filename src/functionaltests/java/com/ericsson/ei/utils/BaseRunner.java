package com.ericsson.ei.utils;

import org.junit.AfterClass;

public class BaseRunner {

    @AfterClass()
    public static void shutdownAmqpBroker() {
        String rabbitMQPort = System.getProperty("rabbitmq.port");
        TestContextInitializer.removeBroker(rabbitMQPort);
    }
}
