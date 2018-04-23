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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRulesHandler {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TestRulesHandler.class);
    private static final String INPUT_FILE_PATH = "src/test/resources/EiffelSourceChangeCreatedEvent.json";
    private static final String OUTPUT_FILE_PATH = "src/test/resources/RulesHandlerOutput.json";
    private static final String RULES_PATH = "src/test/resources/ArtifactRules_new.json";

    private RulesHandler unitUnderTest;

    @Before public void setUp() throws Exception {
        unitUnderTest = new RulesHandler();
        unitUnderTest.setRulePath(RULES_PATH);
        unitUnderTest.init();
    }

    @Test
    public void testPrintJson() {
        String jsonInput = null;
        String jsonOutput;
        RulesObject result;
        RulesObject output = null;
        try {
            jsonInput = FileUtils.readFileToString(new File(INPUT_FILE_PATH), "UTF-8");
            jsonOutput = FileUtils.readFileToString(new File(OUTPUT_FILE_PATH), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            output = new RulesObject(objectmapper.readTree(jsonOutput));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        result = unitUnderTest.getRulesForEvent(jsonInput);
        assertEquals(result, output);
    }
}
