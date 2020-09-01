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
package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.test.utils.TestConfigs;

public class MongoDBHandlerTest {

    final Logger log = LoggerFactory.getLogger(MongoDBHandlerTest.class);

    private MongoDBHandler mongoDBHandler;

    private String dataBaseName = "MongoDBHandlerTestDB";
    private String collectionName = "SampleEvents";
    private String input = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    private String updateInput = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    private String condition = "{\"test_cases.event_id\" : \"testcaseid1\"}";
    
    //Added to test new functionality for EventToObjectMapHandler
    private String conditionForEventToObjectMap = "{\"_id\" : \"testid1\"}";
    private String inputForEventToObjectMap = "{\"_id\": \"testid1\", \"objects\" : [{\"eventid1\", \"eventid2\"}]}";
    private String updateInputForEventToObjectMap = "\"eventid3\"";

    @Before
    public void init() throws Exception {
        TestConfigs.init();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
        mongoDBHandler.insertDocument(dataBaseName, collectionName, input);
        mongoDBHandler.insertDocument(dataBaseName, collectionName, inputForEventToObjectMap);
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
        assertTrue(mongoDBHandler.updateDocument(dataBaseName, collectionName, input, updateInput));
    }

    @After
    public void dropCollection() {
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, condition));
    }
    
    //Added test cases for EventToObjectMapHandler
    @Test
    public void checkDocument() {
    	assertTrue(mongoDBHandler.checkDocumentExists(dataBaseName, collectionName, conditionForEventToObjectMap));
    }
    
    @Test
    public void updateEventToObjectMap() {
    	assertTrue(mongoDBHandler.updateDocumentAddToSet(dataBaseName, collectionName, conditionForEventToObjectMap, updateInputForEventToObjectMap));
    }
}
