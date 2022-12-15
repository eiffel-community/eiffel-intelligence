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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.ericsson.ei.test.utils.TestConfigs;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBHandlerTest {

    private MongoDBHandler mongoDBHandler;

    private String dataBaseName = "MongoDBHandlerTestDB";
    private String collectionName = "SampleEvents";
    private String mapCollectionName = "SampleEventObjectMap";
    private String input = "{\"_id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    private String updateInput = "{\"_id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    private MongoCondition condition = MongoCondition.condition("test_cases.event_id",
            "testcaseid1");

    // Added to test new functionality for EventToObjectMapHandler
    private MongoCondition conditionForEventToObjectMap = MongoCondition.condition("_id",
            "testid1");
    private String inputForEventToObjectMap = "{\"_id\" : \"testid1\", \"objects\" : [\"eventid1\", \"eventid2\"]}";
    private String updateInputForEventToObjectMap = "\"eventid3\"";
    private String inputForEventToObjectMapDuplicate = "{\"_id\" : \"testid1\", \"objects\" : [\"eventid4\"]}";
    private String updateInputForEventToObjectMapDuplicate = "eventid4";
    Document document = Document.parse(inputForEventToObjectMapDuplicate);

    @Before
    public void init() throws Exception {
        TestConfigs.init();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
        mongoDBHandler.insertDocument(dataBaseName, collectionName, input);
        mongoDBHandler.insertDocument(dataBaseName, mapCollectionName, inputForEventToObjectMap);
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
    public void testUpdateDocumentWrongId() {
        final MongoQuery query = MongoCondition.idCondition("id-does-not-exist");
        assertFalse("Document can't be updated",
                mongoDBHandler.updateDocument(dataBaseName, collectionName, query, updateInput));
    }

    @Test
    public void testUpdateDocument() {
        final MongoQuery query = MongoCondition.idCondition("eventId");
        assertTrue("Document was not updated",
                mongoDBHandler.updateDocument(dataBaseName, collectionName, query, updateInput));
    }

    // Added test cases for EventToObjectMapHandler
    @Test
    public void checkDocument() {
        assertTrue(mongoDBHandler.checkDocumentExists(dataBaseName, mapCollectionName,
                conditionForEventToObjectMap));
    }

    @Test
    public void updateEventToObjectMap() {
        assertTrue(mongoDBHandler.updateDocumentAddToSet(dataBaseName, mapCollectionName,
                conditionForEventToObjectMap, updateInputForEventToObjectMap));
    }
    
    @Test
    public void insertEventToObjectMapDuplicate() {
		mongoDBHandler.insertDocumentObject(dataBaseName, mapCollectionName, document, conditionForEventToObjectMap, updateInputForEventToObjectMapDuplicate);
		assertTrue(isEventInEventObjectMap(updateInputForEventToObjectMapDuplicate));
    }
    
    public boolean isEventInEventObjectMap(String eventId) {
    	String condition = "{\"objects\": { \"$in\" : [\"" + eventId + "\"]} }";
        MongoStringQuery query = new MongoStringQuery(condition);
        List<String> documents = mongoDBHandler.find(dataBaseName, mapCollectionName, query);
        return !documents.isEmpty();
    }

    @Test
    public void checkMongoDBStatusUp() {
        assertEquals(mongoDBHandler.checkMongoDbStatus(dataBaseName), true);
    }

    @Test
    public void checkMongoDBStatusDown() {
        final MongoDBHandler mongoDB = new MongoDBHandler();
        final MongoClient mongoclient = null;
        assertEquals(mongoDB.checkMongoDbStatus(dataBaseName), false);
    }
    
    @Test
    public void dropCollection() {
        final MongoCondition idCondition = MongoCondition.idCondition("eventId");
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, idCondition));
        mongoDBHandler.dropCollection(dataBaseName, mapCollectionName);
    }
    
    @Test
    public void testIsMongoDBServerUp() {
        MongoDBHandler mongoDbHandler=mock(MongoDBHandler.class);
        when(mongoDbHandler.isMongoDBServerUp()).thenReturn(true);
        assertTrue(mongoDbHandler.isMongoDBServerUp());
    }

    @Test
    public void testIsMongoDBServerDown() {
      MongoClient client = mock(MongoClient.class);
      MongoDatabase database=mock(MongoDatabase.class);
      when(client.getDatabase(Mockito.anyString())).thenReturn(database);
      mongoDBHandler.setMongoClient(null);
      assertFalse(mongoDBHandler.isMongoDBServerUp());
      //Need to set a working client to enable cleanup
      mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
    }
}
