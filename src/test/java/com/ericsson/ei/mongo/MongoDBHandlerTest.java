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
package com.ericsson.ei.mongo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.test.utils.TestConfigs;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoDBHandlerTest {

    @Rule public TestName name = new TestName();
    final Logger log = LoggerFactory.getLogger(MongoDBHandlerTest.class);

    private MongoDBHandler mongoDBHandler;

    private String dataBaseName = "MongoDBHandlerTestDB";
    private String collectionName = "SampleEvents";
    private String input = "{\"_id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    private String updateInput = "{\"_id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    private MongoCondition condition = MongoCondition.condition("test_cases.event_id",
            "testcaseid1");

    @Before
    public void init() throws Exception {
        TestConfigs.init();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
        mongoDBHandler.insertDocument(dataBaseName, collectionName, input);
    }

    @Test
    public void testGetDocuments() {
        ArrayList<String> documents = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        assertTrue(documents.size() > 0);
    }

    @Test
    public void testGetDocumentsOnCondition() {
        ArrayList<String> documents = mongoDBHandler.find(dataBaseName, collectionName, condition);
        assertTrue(documents.size() > 0);
    }

    @Test
    public void testUpdateDocument() {
        MongoQuery query = MongoCondition.idCondition("eventId");
        assertTrue("Document was not updated",
                mongoDBHandler.updateDocument(dataBaseName, collectionName, query, updateInput));
    }

    @Test
    public void testUpdateDocumentWrongId() {
        MongoQuery query = MongoCondition.idCondition("id-does-not-exist");
        assertFalse("Document can't be updated",
                mongoDBHandler.updateDocument(dataBaseName, collectionName, query, updateInput));
    }

    @Test
    public void testIsMongoDBServerUp() {
        MongoClient client = mock(MongoClient.class);
        when(client.getAddress()).thenReturn(new ServerAddress());
        mongoDBHandler.setMongoClient(client);
        assertTrue(mongoDBHandler.isMongoDBServerUp());

        doThrow(Exception.class).when(client).getAddress();
        mongoDBHandler.setMongoClient(client);
        assertFalse(mongoDBHandler.isMongoDBServerUp());

        // Need to set a working client to enable cleanup
        mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
    }

    @After
    public void dropCollection() {
        MongoCondition idCondition = MongoCondition.idCondition("eventId");
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, idCondition));
    }
}
