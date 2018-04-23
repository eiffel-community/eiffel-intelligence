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
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRulesObject {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TestRulesObject.class);
    private static final String INPUT_FILE_PATH = "src/test/resources/RulesHandlerOutput2.json";
    private static final String INPUT_RULES_PATH = "src/test/resources/ProcessRules.json";

    private RulesObject unitUnderTest;
    private JsonNode rulesJson;

    @Test
    public void testPrintJson() {
        String expectedOutput = "{ id : meta.id, type : meta.type, time : meta.time, gav : data.gav, fileInformation "
                + ": data.fileInformation, buildCommand : data.buildCommand }";
        String result;
        try {
            String rulesString = FileUtils.readFileToString(new File(INPUT_FILE_PATH), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            rulesJson = objectmapper.readTree(rulesString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        unitUnderTest = new RulesObject(rulesJson);
        result = unitUnderTest.getExtractionRules();
        assertEquals(result, expectedOutput);
    }

    @Test
    public void fetchProcessRulesTest() {
        String expectedOutput = "{testCaseExecutions :[{testCaseDuration : diff(testCaseExecutions[0].testCaseFinishedTime, testCaseExecutions[0].testCaseStartedTime)}]}";
        String result;
        try {
            String ruleString = FileUtils.readFileToString(new File(INPUT_RULES_PATH), "UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            rulesJson = objectMapper.readTree(ruleString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        unitUnderTest = new RulesObject(rulesJson);
        result = unitUnderTest.fetchProcessRules();
        assertEquals(result, expectedOutput);
    }

}
