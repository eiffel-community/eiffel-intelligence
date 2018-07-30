package com.ericsson.ei;

import com.ericsson.ei.handlers.test.ObjectHandlerTest;
import com.ericsson.ei.mongoDBHandler.test.MongoDBHandlerTest;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.queryservice.test.QueryServiceTest;
import com.ericsson.ei.rules.test.TestRulesService;
import com.ericsson.ei.subscription.SubscriptionServiceTest;
import com.ericsson.ei.subscriptionhandler.test.SubscriptionHandlerTest;
import com.ericsson.ei.subscriptionhandler.test.SubscriptionRepeatDbHandlerTest;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestRulesService.class,
        MongoDBHandlerTest.class,
        SubscriptionHandlerTest.class,
        SubscriptionRepeatDbHandlerTest.class,
        ObjectHandlerTest.class,
        QueryServiceTest.class,
        SubscriptionServiceTest.class})
public class SuiteCasesForMongo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuiteCasesForMongo.class);
    private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;

    @Autowired
    public MongoDBHandler mongoDBHandler;

    @BeforeClass
    public static void init() {
        try {
            testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @AfterClass
    public static void close() {
        mongoClient.close();
        testsFactory.shutdown();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
    }
}