package com.ericsson.ei;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
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
    private static final String DB_NAME = "eiffel_intelligence";
    private static final String MISSED_NOTIFICATION = "MissedNotification";
    private static List<MongoClient> mongoClients = new LinkedList<>();

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

    public synchronized static MongoClient borrow() {
        MongoClient mongoClient;
        if (mongoClients.size() == 0) {
            mongoClient = createConn();
            mongoClients.add(mongoClient);
        }
        mongoClient = mongoClients.get(0);
        mongoClients.remove(mongoClient);
        return mongoClient;
    }

    public static void returnMongoClient(MongoClient client) {
        MongoDatabase mongoDatabase = client.getDatabase(DB_NAME);
        mongoDatabase.drop();
        mongoDatabase = client.getDatabase(MISSED_NOTIFICATION);
        mongoDatabase.drop();
        mongoClients.add(client);
    }

    @PreDestroy
    public void shutdown() {
        IntStream.range(0, mongoClients.size() - 1).mapToObj(i -> mongoClients.get(i)).forEachOrdered(Mongo::close);
    }
}