package com.ericsson.ei.flowtests;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;

public class FlowTestConfigs {

    private AMQPBrokerManager amqpBroker;
    private MongodForTestsFactory testsFactory;
    private Queue queue = null;
    private RabbitAdmin admin;
    private ConnectionFactory cf;

    final static Logger LOGGER = (Logger) LoggerFactory.getLogger(FlowTestConfigs.class);

    @Getter
    private Connection conn;

    @Getter
    private MongoClient mongoClient = null;

    public void init() throws Exception {
        setUpMessageBus();
        setUpEmbeddedMongo();
    }

    private void setUpMessageBus() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "100");

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

    private void setUpEmbeddedMongo() throws IOException {
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
        if (amqpBroker != null) {
            amqpBroker.stopBroker();
        }
        try {
            conn.close();
        } catch (Exception e) {
            // We try to close the connection but if
            // the connection is closed we just receive the
            // exception and go on
        }

        if (mongoClient != null)
            mongoClient.close();
        if (testsFactory != null)
            testsFactory.shutdown();

    }

    void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        admin = new RabbitAdmin(ccf);
        queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }

}