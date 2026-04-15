package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;

import java.util.Base64;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.TestSocketUtils;

import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;

public class TestConfigs {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfigs.class);

    private static MongoClient mongoClient = null;

    @Getter
    private static AMQPBrokerManager amqpBroker;

    protected static void createAmqpBroker() throws Exception {
        if (amqpBroker != null) {
            return;
        }
        // Generates a random port for amqpBroker and starts up a new broker
        int port = TestSocketUtils.findAvailableTcpPort();

        System.setProperty("rabbitmq.port", Integer.toString(port));
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.resend.initial.delay", "500");
        System.setProperty("waitlist.resend.fixed.rate", "3000");

        String config = "src/functionaltests/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), port);

        LOGGER.debug("Started embedded message bus for tests on port: " + port);
        amqpBroker.startBroker();
    }

    protected static void startUpMongoClient() throws IOException {
        if (mongoClient != null) {
            return;
        }

        try {
            final ListDatabasesIterable<Document> list = mongoClient.listDatabases();
            final MongoCursor<Document> iter = list.iterator();
            final String port = "" + iter.getServerAddress().getPort();
            System.setProperty("spring.mongodb.port", port);
            LOGGER.debug("Started embedded Mongo DB for tests on port: " + port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected static void setAuthorization() {
        String password = new String(Base64.getEncoder().encode("password".getBytes()), java.nio.charset.StandardCharsets.UTF_8);
        System.setProperty("ldap.password", password);
    }

    protected void setRules() {
        System.setProperty("rules", " /rules/ArtifactRules-Eiffel-Agen-Version.json");
    }
}