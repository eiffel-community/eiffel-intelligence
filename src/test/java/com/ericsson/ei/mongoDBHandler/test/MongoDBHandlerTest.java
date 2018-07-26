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
package com.ericsson.ei.mongoDBHandler.test;

import com.ericsson.ei.App;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
public class MongoDBHandlerTest {

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private String dataBaseName = "EventStorageDBbbb";
    private String collectionName = "SampleEvents";
    private String input = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    private String updateInput = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    private String condition = "{\"test_cases.event_id\" : \"testcaseid1\"}";

    @PostConstruct
    public void initMocks() {
        assertTrue(mongoDBHandler.insertDocument(dataBaseName, collectionName, input));
    }

    @Test
    public void testGetDocuments() {
        List<String> documents = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        assertTrue(documents.size() > 0);
    }

    @Test
    public void testGetDocumentsOnCondition() {
        List<String> documents = mongoDBHandler.find(dataBaseName, collectionName, condition);
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
}
