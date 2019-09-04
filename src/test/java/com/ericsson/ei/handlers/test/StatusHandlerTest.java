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
package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.handlers.StatusHandler;
import com.ericsson.ei.status.Status;
import com.fasterxml.jackson.databind.JsonNode;

@RunWith(MockitoJUnitRunner.class)
public class StatusHandlerTest {

    private static final String MONGO_DB_STATUS_KEY = "mongoDBStatus";
    private static final String EIFFEL_INTELLIGENCE_STATUS_KEY = "eiffelIntelligenceStatus";
    private static final String RABBITMQ_STATUS_KEY = "rabbitMQStatus";

    @Mock
    private MongoDBHandler mongoDBHandlerMock;

    @Mock
    private RmqHandler rmqHandlerMock;

    @InjectMocks
    private StatusHandler statusHandler;

    @Test
    public void testStatusHandlerWithStatusDataAsDefault() {
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("Mongo DB default value should be NOT_SET", Status.NOT_SET.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
    }

    @Test
    public void testStatusHandlerWithStatusMongoDBAvailable() {
        when(mongoDBHandlerMock.isMongoDBServerUp()).thenReturn(true);
        when(rmqHandlerMock.isRabbitMQServerUp()).thenReturn(true);

        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("MongoDB should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
        assertEquals("RabbitMQ should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(RABBITMQ_STATUS_KEY).asText());
        assertEquals("EI should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(EIFFEL_INTELLIGENCE_STATUS_KEY).asText());
    }

    @Test
    public void testStatusHandlerWithStatusMongoDBUnavailable() {
        when(mongoDBHandlerMock.isMongoDBServerUp()).thenReturn(false);
        when(rmqHandlerMock.isRabbitMQServerUp()).thenReturn(true);

        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("MongoDB should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
        assertEquals("EI should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(EIFFEL_INTELLIGENCE_STATUS_KEY).asText());
        assertEquals("RabbitMQ should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(RABBITMQ_STATUS_KEY).asText());
    }

    @Test
    public void testStatusHandlerWithStatusRabbitMQUnavailable() {
        when(mongoDBHandlerMock.isMongoDBServerUp()).thenReturn(true);
        when(rmqHandlerMock.isRabbitMQServerUp()).thenReturn(false);

        statusHandler.run();
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("MongoDB should be AVAILABLE", Status.AVAILABLE.name(),
                statusData.get(MONGO_DB_STATUS_KEY).asText());
        assertEquals("EI should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(EIFFEL_INTELLIGENCE_STATUS_KEY).asText());
        assertEquals("RabbitMQ should be UNAVAILABLE", Status.UNAVAILABLE.name(),
                statusData.get(RABBITMQ_STATUS_KEY).asText());
    }
}
