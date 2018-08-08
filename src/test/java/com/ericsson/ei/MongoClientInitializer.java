package com.ericsson.ei;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MongoClientInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientInitializer.class);

    static {
        try {
            MongodForTestsFactory testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            MongoClient mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}