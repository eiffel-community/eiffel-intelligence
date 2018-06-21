/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.utils;

import com.ericsson.ei.App;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * @author evasiba
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FunctionalTestBase.class })
public class FunctionalTestBase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalTestBase.class);

    @Autowired
    private MongoProperties mongoProperties;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String collection;

    private MongoClient mongoClient;
    
    public int getMongoDbPort() {
        return mongoProperties.getPort();
    }
    
    public String getMongoDbHost() {
        return mongoProperties.getHost();
    }
    
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        int debug = 1;
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        int debug = 1;
    }

    public boolean waitForEventsToBeProcessed(int eventsCount) {
        int maxTime = 30;
        int counterTime = 0;
        long processedEvents = 0;
        while (processedEvents < eventsCount && counterTime < maxTime) {
            processedEvents = countProcessedEvents(database, collection);
            LOGGER.debug("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
                counterTime += 3;
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return (processedEvents == eventsCount);
    }
    
    private long countProcessedEvents(String database, String collection) {
        mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> table = db.getCollection(collection);
        return table.count();
    }

    
}