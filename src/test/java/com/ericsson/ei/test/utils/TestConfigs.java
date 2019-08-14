package com.ericsson.ei.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

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

import com.ericsson.ei.utils.AMQPBrokerManager;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;

public class TestConfigs {

    @Getter
    private static AMQPBrokerManager amqpBroker;
    private static MongodForTestsFactory testsFactory;

    @Getter
    private static ConnectionFactory connectionFactory;

    final static Logger LOGGER = LoggerFactory.getLogger(TestConfigs.class);

    @Getter
    private static Connection connection;

    @Getter
    private static MongoClient mongoClient = null;

    public static synchronized void init() throws Exception {
        setUpMessageBus();
        setUpEmbeddedMongo();
    }

    private static synchronized void setUpMessageBus() throws Exception {
        LOGGER.debug("Debug:setting up message buss");

        LOGGER.debug("before setting up message buss: amqpBroker: " + amqpBroker + ", conn: " + connection + ",cf:"
                + connectionFactory);
        if (amqpBroker != null || connection != null || connectionFactory != null) {
            return;
        }

        int port = SocketUtils.findAvailableTcpPort();
        setSystemProperties(port);
        LOGGER.info("setting up message bus...");
        setupBroker(port);

        setupConnectionFactory(port);
        LOGGER.debug("Setting up message bus done!");
    }

    public static MongoClient mongoClientInstance() throws Exception {
        if (mongoClient == null) {
            setUpEmbeddedMongo();
        }
        return mongoClient;
    }

    private static synchronized void setUpEmbeddedMongo() throws IOException {
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

    public static void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(connectionFactory);
        LOGGER.info("Creating exchange: {} and queue: {}", exchangeName, queueName);
        final RabbitAdmin admin = new RabbitAdmin(ccf);
        final Queue queue = new Queue(queueName, false);
        final TopicExchange exchange = new TopicExchange(exchangeName);

        admin.declareQueue(queue);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }

    protected static void setAuthorization() {
        String password = StringUtils.newStringUtf8(Base64.encodeBase64("password".getBytes()));
        System.setProperty("ldap.password", password);
    }

    protected void setRules() {
        System.setProperty("rules", " /rules/ArtifactRules-Eiffel-Agen-Version.json");
    }

    protected static void setSystemProperties(int port) {
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "100");
        LOGGER.debug("done setting up message buss properties");
    }

    protected static void setupBroker(int port) throws Exception {
        String config = "src/test/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), port);
        amqpBroker.startBroker();
    }

    protected static void setupConnectionFactory(int port) throws IOException, TimeoutException {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(port);
        connectionFactory.setHandshakeTimeout(600000);
        connectionFactory.setConnectionTimeout(600000);
        connection = connectionFactory.newConnection();
    }
}