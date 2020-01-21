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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.ei.exception.ReplacementMarkerException;
import com.ericsson.ei.rules.MatchIdRulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MatchIdRulesHandlerTest {
    private static final String INPUT_FILE_PATH = "src/test/resources/MatchIdRulesHandlerInput.json";
    private static final String OUTPUT_FILE_PATH = "src/test/resources/MatchIdRulesHandlerOutput.json";
    private static final String EVENT_ID = "e90daae3-bf3f-4b0a-b899-67834fd5ebd0";

    private MatchIdRulesHandler matchIdRulesHandler;

    @Test
    public void replaceIdInRulesTest() throws IOException, ReplacementMarkerException {
        setMatchingProperty();
        String jsonInput = readFile(INPUT_FILE_PATH);
        String jsonOutput = readFile(OUTPUT_FILE_PATH);
        RulesObject ruleObject = createRulesObject(jsonInput);
        String matchIdString = ruleObject.getMatchIdRules();
        String replacedId = matchIdRulesHandler.replaceIdInRules(matchIdString, EVENT_ID);
        assertEquals(replacedId, jsonOutput);
    }

    @Test(expected = ReplacementMarkerException.class)
    public void replaceIdInRulesExceptionTest() throws IOException, ReplacementMarkerException {
        setMismatchingProperty();
        String jsonInput = readFile(INPUT_FILE_PATH);
        RulesObject ruleObject = createRulesObject(jsonInput);
        String matchIdString = ruleObject.getMatchIdRules();
        matchIdRulesHandler.replaceIdInRules(matchIdString, EVENT_ID);
    }

    private RulesObject createRulesObject(String input) throws IOException {
        ObjectMapper objectmapper = new ObjectMapper();
        RulesObject ruleObject = new RulesObject(objectmapper.readTree(input));
        return ruleObject;
    }

    private String readFile(String path) throws IOException {
        return FileUtils.readFileToString(new File(path), "UTF-8");
    }

    private void setMatchingProperty() {
        matchIdRulesHandler = new MatchIdRulesHandler();
        Whitebox.setInternalState(matchIdRulesHandler, "replacementMarker",
                "%IdentifyRulesEventId%");
    }

    private void setMismatchingProperty() {
        matchIdRulesHandler = new MatchIdRulesHandler();
        Whitebox.setInternalState(matchIdRulesHandler, "replacementMarker", "%IdentifyRulesEvent%");
    }
}
