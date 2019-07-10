package com.ericsson.ei.testsuite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ericsson.ei.subscription.SubscriptionHandlerTest;
import com.ericsson.ei.subscription.SubscriptionValidatorTest;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

//@RunWith(ConcurrentSuite.class)
@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({ SubscriptionValidatorTest.class, SubscriptionHandlerTest.class, })

public class SuiteTest {

    private static MongoPool mongoPool;
    private static List<MongoClient> clients = new ArrayList<>();
    private static LinkedList<MongoClient> stackOfMongoClients = new LinkedList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        final int numOfMongos = 2;
        final MongodForTestsFactory mongodTestFactory = new MongodForTestsFactory();

        for (int i = 1; i <= numOfMongos; i++) {
            // final MongodForTestsFactory mongodTestFactory = new MongodForTestsFactory();
            final MongoClient newClient = mongodTestFactory.newMongo();
            clients.add(newClient);
            stackOfMongoClients.add(newClient);
        }
        System.out.println(stackOfMongoClients.size());
        System.out.println(clients.get(0));
        System.out.println(stackOfMongoClients.pop());
        System.out.println(stackOfMongoClients.size());
        System.out.println(stackOfMongoClients.pop());
        System.out.println(getMongoClients().isEmpty());
        // mongoInstances = mongoPool.getMongoPool(allTestClasses);
    }

    public static List<MongoClient> getMongo() {
        return clients;
    }

    public static LinkedList<MongoClient> getMongoClients() {
        return stackOfMongoClients;
    }

}
