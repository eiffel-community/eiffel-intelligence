package com.ericsson.ei.utils;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;

public class TestConfigs {

    private static ConnectionFactory cf;
    private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(TestConfigs.class);

    @Getter
    private Connection conn;

    @Getter
    private MongoClient mongoClient = null;

    @Bean
    void amqpBroker() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "3000");

        String config = "src/functionaltests/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        AMQPBrokerManager amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), port);
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

    @Bean
    void mongoClient() {
        try {
            MongodForTestsFactory testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
            LOGGER.debug("Started embedded Mongo DB for tests on port: " + port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Bean
    void setAuthorization() {
        String password = StringUtils.newStringUtf8(Base64.encodeBase64("password".getBytes()));
        System.setProperty("ldap.enabled", "true");
        System.setProperty("ldap.url", "ldap://ldap.forumsys.com:389/dc=example,dc=com");
        System.setProperty("ldap.base.dn", "dc=example,dc=com");
        System.setProperty("ldap.username", "cn=read-only-admin,dc=example,dc=com");
        System.setProperty("ldap.password", password);
        System.setProperty("ldap.user.filter", "uid={0}");
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