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
package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.IdRulesHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestIdRulesHandler {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TestRulesHandler.class);
    private static final String RULES_PATH = "src/test/resources/ArtifactRules_new.json";
    private static final String EVENT_PATH = "src/test/resources/EiffelArtifactCreatedEvent.json";

    @Test
    public void testGetIds(){
        RulesObject rulesObject = null;
        String eventFile = "";
        String expectedIds = "e90daae3-bf3f-4b0a-b899-67834fd5ebd0";

        IdRulesHandler idRulesHandler = new IdRulesHandler();
        JmesPathInterface jmespath = new JmesPathInterface();
        idRulesHandler.setJmesPathInterface(jmespath);
        try{
            String jsonRules = FileUtils.readFileToString(new File(RULES_PATH), "UTF-8");
            ObjectMapper rulesObjectMapper = new ObjectMapper();
            eventFile = FileUtils.readFileToString(new File(EVENT_PATH), "UTF-8");
            rulesObject = new RulesObject(rulesObjectMapper.readTree(jsonRules.replace("[", "").replace("]", "")));
            LOGGER.debug("RulesObject: {}", rulesObject.getJsonRulesObject());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        JsonNode ids = idRulesHandler.getIds(rulesObject, eventFile);
        LOGGER.debug("Ids: {}", ids.textValue());

        assertEquals(expectedIds, ids.textValue());
    }
}
