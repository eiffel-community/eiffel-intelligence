package com.ericsson.ei.flowtests;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.SocketUtils;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;

public class TestConfigs {

    private static AMQPBrokerManager amqpBroker;
    private static MongodForTestsFactory testsFactory;
//    private Queue queue = null;
//    private RabbitAdmin admin;
    private static ConnectionFactory cf;

    final static Logger LOGGER = LoggerFactory.getLogger(TestConfigs.class);

    @Getter
    private static Connection conn;

    @Getter
    private static MongoClient mongoClient = null;

    public static void init() throws Exception {
        setUpMessageBus();
        setUpEmbeddedMongo();
    }

    private static void setUpMessageBus() throws Exception {
        LOGGER.debug("setting up message buss");
        if (amqpBroker != null || conn != null || cf != null) {
            return;
        }

        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "100");
        LOGGER.info("setting up message buss");
        String config = "src/test/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), port);
        amqpBroker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");

        cf.setPort(port);
        cf.setHandshakeTimeout(600000);
        cf.setConnectionTimeout(600000);
        conn = cf.newConnection();

    }

    public static MongoClient mongoClientInstance() throws Exception {
        if (mongoClient == null) {
            setUpEmbeddedMongo();
        }

        return mongoClient;
    }

    private static void setUpEmbeddedMongo() throws IOException {
        if (mongoClient != null) {
            return;
        }

        try {
            testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void tearDown() {
//        if (amqpBroker != null) {
//            amqpBroker.stopBroker();
//        }
//        try {
//            conn.close();
//        } catch (Exception e) {
//            // We try to close the connection but if
//            // the connection is closed we just receive the
//            // exception and go on
//        }
//
//        if (mongoClient != null)
//            mongoClient.close();
//        if (testsFactory != null)
//            testsFactory.shutdown();

    }

    public static void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        LOGGER.info("Creating exchange: {} and queue: {}", exchangeName, queueName);
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }

}