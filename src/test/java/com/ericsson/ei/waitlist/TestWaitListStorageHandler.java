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
package com.ericsson.ei.waitlist;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestWaitListStorageHandler {

    WaitListStorageHandler waitListStorageHandler = new WaitListStorageHandler();
    JmesPathInterface jmespath = new JmesPathInterface();

    @Mock
    MongoDBHandler mongoDBHandler;
    static Logger log = (Logger) LoggerFactory.getLogger(TestWaitListStorageHandler.class);
    private final String rulesPath = "src/test/resources/WaitlistStorageHandlerRule.json";
    private final String eventPath = "src/test/resources/EiffelArtifactCreatedEvent.json";
    private final String inputJson1="src/test/resources/testWaitListinput1.json";
    
    ArrayList<String> output=new ArrayList<String>();

    @Before
    public void init() throws IOException{
        MockitoAnnotations.initMocks(this);
        output.add(FileUtils.readFileToString(new File(inputJson1), "UTF-8"));
        Mockito.when(mongoDBHandler.getAllDocuments(Mockito.anyString(), Mockito.anyString())).thenReturn(output);
        Mockito.when(mongoDBHandler.insertDocument(Mockito.anyString(), Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        waitListStorageHandler.setMongoDbHandler(mongoDBHandler);
        waitListStorageHandler.setJmesPathInterface(jmespath);
    }

    @Test
    public void testAddEventToWaitList(){
        String jsonRule = null;
        String eventFile = null;
        RulesObject rulesObject = null;
        try{
            jsonRule = FileUtils.readFileToString(new File(rulesPath), "UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            eventFile = FileUtils.readFileToString(new File(eventPath), "UTF-8");
            rulesObject = new RulesObject(objectMapper.readTree(jsonRule));
            waitListStorageHandler.addEventToWaitList(eventFile, rulesObject);
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
        }
    }

    @Test
    public void testGetWaitListEvents(){
        ArrayList<String> documents = waitListStorageHandler.getWaitList();
        assertTrue(documents.size() > 0);
    }

}