package com.ericsson.ei.utils;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
import java.io.IOException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
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

public class TestConfigs {

    private static AMQPBrokerManager amqpBroker;
    private static ConnectionFactory cf;
    private Connection conn;
    private MongodForTestsFactory testsFactory;
    private MongoClient mongoClient = null;

    final static Logger LOGGER = (Logger) LoggerFactory.getLogger(TestConfigs.class);

    public void amqpBroker() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "3000");

        String config = "src/functionaltests/resources/configs/qpidConfig.json";
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
        LOGGER.debug("Started embedded message bus for tests on port: " + port);
    }

    public void mongoClient() throws IOException {
        try {
            testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
            LOGGER.debug("Started embedded Mongo DB for tests on port: " + port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setAuthorization() {
        String password = StringUtils.newStringUtf8(Base64.encodeBase64("password".getBytes()));
        System.setProperty("ldap.enabled", "true");
        System.setProperty("ldap.url", "ldap://ldap.forumsys.com:389/dc=example,dc=com");
        System.setProperty("ldap.base.dn", "dc=example,dc=com");
        System.setProperty("ldap.username", "cn=read-only-admin,dc=example,dc=com");
        System.setProperty("ldap.password", password);
        System.setProperty("ldap.user.filter", "uid={0}");
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

    public void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }
}