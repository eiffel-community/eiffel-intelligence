package com.ericsson.ei;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MongoClientInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientInitializer.class);

    @Getter
    private static MongoClient mongoClient;

    static {
        try {
            MongodForTestsFactory testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}