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
package com.ericsson.ei.matchidruleshandlertest;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MatchIdRuleshandlerTest {
    private final String inputFilePath = "src/test/resources/MatchIdRulesHandlerInput.json";
    private final String outputFilePath = "src/test/resources/MatchIdRulesHandlerOutput.json";
    private final String id = "e90daae3-bf3f-4b0a-b899-67834fd5ebd0";

    static Logger log = (Logger) LoggerFactory.getLogger(MatchIdRuleshandlerTest.class);

    @Test
    public void replaceIdInRulesTest() {
        String jsonInput = null;
        String jsonOutput = null;
        RulesObject ruleObject = null;
        try {
            jsonInput = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
            jsonOutput = FileUtils.readFileToString(new File(outputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            ruleObject = new RulesObject(objectmapper.readTree(jsonInput));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String matchIdString = ruleObject.getMatchIdRules();
        String replacedId = MatchIdRulesHandler.replaceIdInRules(matchIdString, id);
        assertEquals(replacedId.toString(), jsonOutput.toString());
    }

}
