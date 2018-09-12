package com.ericsson.ei.utils;

import com.mongodb.MongoClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class TestConfigs {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfigs.class);

    private MongoClient mongoClient = null;

    protected static Map<Integer, AMQPBrokerManager> amqpBrokerMap = new HashMap<>();

    AMQPBrokerManager createAmqpBroker() throws Exception {
        // Generates a random port for amqpBroker and starts up a new broker
        int port = SocketUtils.findAvailableTcpPort();

        System.setProperty("rabbitmq.port", Integer.toString(port));
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "500");
        System.setProperty("waitlist.fixedRateResend", "3000");

        String config = "src/functionaltests/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        AMQPBrokerManager amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), Integer.toString(port));

        LOGGER.debug("Started embedded message bus for tests on port: " + port);
        amqpBroker.startBroker();

        // add new amqp broker to pool
        amqpBrokerMap.put(port, amqpBroker);

        return amqpBroker;
    }

    void startUpMongoClient() throws IOException {
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

    void setAuthorization() {
        String password = StringUtils.newStringUtf8(Base64.encodeBase64("password".getBytes()));
        System.setProperty("ldap.enabled", "true");
        System.setProperty("ldap.url", "ldap://ldap.forumsys.com:389/dc=example,dc=com");
        System.setProperty("ldap.base.dn", "dc=example,dc=com");
        System.setProperty("ldap.username", "cn=read-only-admin,dc=example,dc=com");
        System.setProperty("ldap.password", password);
        System.setProperty("ldap.user.filter", "uid={0}");
    }

    public static AMQPBrokerManager getBroker(int port) {
        return amqpBrokerMap.get(port);
    }

    public static void removeBroker(String port) {
        AMQPBrokerManager broker = amqpBrokerMap.get(Integer.parseInt(port));
        broker.stopBroker();
        amqpBrokerMap.remove(port);
    }
}