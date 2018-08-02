package com.ericsson.ei;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class MongoClientInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientInitializer.class);

    @Getter
    private static MongoClient mongoClient;

    static {
        try {
            MongodForTestsFactory testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void mongoClient() {
        String port = "" + mongoClient.getAddress().getPort();
        System.setProperty("spring.data.mongodb.port", port);
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        mongoClient();
    }
}
