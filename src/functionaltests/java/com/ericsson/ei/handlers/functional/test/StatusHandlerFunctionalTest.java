/*
   Copyright 2019 Ericsson AB.
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
package com.ericsson.ei.handlers.functional.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.StatusHandler;
import com.ericsson.ei.status.Status;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: StatusHandlerFunctionalTest",
        "missedNotificationDataBaseName: StatusHandlerFunctionalTest-missedNotifications",
        "rabbitmq.exchange.name: StatusHandlerFunctionalTest-exchange",
        "rabbitmq.consumerName: StatusHandlerFunctionalTest-consumer" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class StatusHandlerFunctionalTest {

    private static final String CONNECTIONS_KEY_AVAILABLE = "available";
    private static final String CONNECTIONS_KEY_CURRENT = "current";

    private static final String EVENT_REPOSITORY_STATUS_KEY = "eventRepositoryStatus";
    private static final String MONGO_DB_STATUS_KEY = "mongoDBStatus";
    private static final String EIFFEL_INTELLIGENCE_STATUS_KEY = "eiffelIntelligenceStatus";

    @Mock
    private MongoDBHandler mongoDBHandlerMock;
    @Mock
    private MongoClient mongoClientMock;
    @Mock
    private MongoDatabase databaseMock;
    @Mock
    private Document serverStatusMock;

    Map<String, Integer> connections;

    @Autowired
    private StatusHandler statusHandler;

    @Before
    public void beforeTests() throws IOException {
        connections = new HashMap<String, Integer>();
        connections.put(CONNECTIONS_KEY_AVAILABLE, 20);
        connections.put(CONNECTIONS_KEY_CURRENT, 10);

        // Stubbing MondoDBHandler to be able to change status.
        when(mongoDBHandlerMock.getMongoClient()).thenReturn(mongoClientMock);
        when(mongoClientMock.getDatabase("admin")).thenReturn(databaseMock);
        when(databaseMock.runCommand(Mockito.any())).thenReturn(serverStatusMock);
        when(serverStatusMock.get(Mockito.any())).thenReturn(connections);
        statusHandler.setMongoDBHandler(mongoDBHandlerMock);
    }

    @Test
    public void testStatusHandlerWithStatusEventRepositoryDisabled() {
        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("Event repository should be DISABLED", Status.DISABLED.name(),
                statusData.get(EVENT_REPOSITORY_STATUS_KEY).asText());
    }

    @Test
    public void testStatusHandlerWithStatusMongoDBAvailable() {
        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("MongoDB should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
        assertEquals("EI should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(EIFFEL_INTELLIGENCE_STATUS_KEY).asText());
    }

    @Test
    public void testStatusHandlerWithStatusMongoDBUnavailable() {
        connections.put(CONNECTIONS_KEY_AVAILABLE, 0);
        connections.put(CONNECTIONS_KEY_CURRENT, 10);
        when(serverStatusMock.get(Mockito.any())).thenReturn(connections);
        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("MongoDB should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
        assertEquals("EI should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(EIFFEL_INTELLIGENCE_STATUS_KEY).asText());
    }

}
