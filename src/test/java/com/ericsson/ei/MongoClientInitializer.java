package com.ericsson.ei;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MongoClientInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientInitializer.class);
    private static List<MongoClient> mongoClients = new LinkedList<>();

    static {
        IntStream.range(0, 5).forEachOrdered(i -> mongoClients.add(createConn()));
    }

    private static MongoClient createConn() {
        MongoClient mongoClient = null;
        try {
            MongodForTestsFactory testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return mongoClient;
    }

    public static MongoClient borrow() {
        MongoClient mongoClient = mongoClients.get(1);
        mongoClients.remove(mongoClient);
        return mongoClient;
    }

    public static void returnMongoClient(MongoClient client) {
        mongoClients.add(client);
    }

    @PreDestroy
    public void shutdown() {
        IntStream.range(0, 5).mapToObj(i -> mongoClients.get(i)).forEachOrdered(Mongo::close);
    }
}