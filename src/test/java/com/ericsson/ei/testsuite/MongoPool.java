package com.ericsson.ei.testsuite;

import java.util.LinkedList;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class MongoPool {

    private static MongoPool mongoPool;
    private static LinkedList<MongoClient> stackOfMongoClients = new LinkedList<>();

    public static void mongoInstances() throws Exception {
        final int numOfMongos = 2;
        final MongodForTestsFactory mongodTestFactory = new MongodForTestsFactory();

        for (int i = 1; i <= numOfMongos; i++) {
            // final MongodForTestsFactory mongodTestFactory = new MongodForTestsFactory();
            final MongoClient newClient = mongodTestFactory.newMongo();
            stackOfMongoClients.add(newClient);
        }
        System.out.println(stackOfMongoClients.size());
        System.out.println(stackOfMongoClients.pop());
        System.out.println(stackOfMongoClients.size());
        System.out.println(stackOfMongoClients.pop());
        System.out.println(getMongoClients().isEmpty());
        // mongoInstances = mongoPool.getMongoPool(allTestClasses);
    }

    public static LinkedList<MongoClient> getMongoClients() {
        return stackOfMongoClients;
    }

}

