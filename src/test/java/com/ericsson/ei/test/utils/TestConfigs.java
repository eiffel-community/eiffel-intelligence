package com.ericsson.ei.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.utils.AMQPBrokerManager;
import com.mongodb.client.ListDatabasesIterable;
//import com.mongodb.MongoClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;

public class TestConfigs {

    private static final String DEFAULT_MONGO_URI = "mongodb://localhost:27017";
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
        setUpEmbeddedMongo(DEFAULT_MONGO_URI);
    }

    private static synchronized void setUpMessageBus() throws Exception {
        LOGGER.debug("Before setting up message bus, amqp broker: " + amqpBroker + ", connection: "
                + connection + ",connection factory:"
                + connectionFactory);
        if (amqpBroker != null || connection != null || connectionFactory != null) {
            return;
        }

        int port = SocketUtils.findAvailableTcpPort();
        setSystemProperties(port);
        setupBroker(port);

        setupConnectionFactory(port);
        LOGGER.debug("Setting up message bus done!");
    }

    public static MongoClient mongoClientInstance(String mongoUri) throws Exception {
        if (mongoClient == null) {
            setUpEmbeddedMongo(mongoUri);
        }
        return mongoClient;
    }

    private static synchronized void setUpEmbeddedMongo(String mongoUri) throws IOException {
        if (mongoClient != null) {
            return;
        }

        try {
            String port = mongoUri.substring(mongoUri.length()-5);
            setNewPortToMongoDBUriProperty(mongoUri, port);
        } catch (Exception e) {
            LOGGER.error("Error setting new mongoDB uri property {}", e.getMessage(), e);
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

    protected void setRules() {
        System.setProperty("rules", " /rules/ArtifactRules-Eiffel-Agen-Version.json");
    }

    protected static void setSystemProperties(int port) {
        System.setProperty("rabbitmq.port", "" + port);
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.resend.initial.delay", "500");
        System.setProperty("waitlist.resend.fixed.rate", "100");
        LOGGER.info("Message bus port:{}", port);
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

    private static void setNewPortToMongoDBUriProperty(String mongoUri, String port) {
        String modifiedUri = mongoUri.replaceAll("[0-9]{4,5}", port);
        System.setProperty("spring.data.mongodb.uri", modifiedUri);
        LOGGER.debug("System property 'spring.data.mongodb.uri' changed from '{}' to '{}'",
                mongoUri, modifiedUri);
    }
}