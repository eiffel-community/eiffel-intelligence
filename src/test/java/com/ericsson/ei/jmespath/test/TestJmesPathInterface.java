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
package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestJmesPathInterface {
    private JmesPathInterface unitUnderTest = new JmesPathInterface();
    ObjectMapper mapper = new ObjectMapper();

    private static final String inputFilePath = "src/test/resources/EiffelArtifactCreatedEvent.json";
    private static final String outputFilePath = "src/test/resources/JmesPathInterfaceOutput.json";
    private static final String inputDiffpath = "src/test/resources/DiffFunctionInput.json";
    private static final String extractionRuleFilePath = "src/test/resources/ExtractionRule.txt";

    private static String jsonInput = "";
    private static String extractionRulesTest = "";
    private static String jsonOutput = "";
    private static String jsonInputDiff = "";

    @BeforeClass
    public static void beforeClass() throws Exception {
        jsonInput = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
        jsonOutput = FileUtils.readFileToString(new File(outputFilePath), "UTF-8");
        extractionRulesTest = FileUtils.readFileToString(new File(extractionRuleFilePath), "UTF-8");
        jsonInputDiff = FileUtils.readFileToString(new File(inputDiffpath), "UTF-8");
    }

    @Test
    public void testRunRuleOnEvent() throws Exception {
        JsonNode output = mapper.readTree(jsonOutput);
        JsonNode result = unitUnderTest.runRuleOnEvent(extractionRulesTest, jsonInput);
        assertEquals(output, result);
    }

    @Test
    public void testDiffFunction() throws Exception {
        JsonNode expectedResult = mapper.readTree("{\"testCaseExecutions\":[{\"testCaseDuration\":6.67}]}");
        String processRule = "{testCaseExecutions :[{testCaseDuration : diff(testCaseExecutions[0].testCaseFinishedTime, testCaseExecutions[0].testCaseStartedTime)}]}";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInputDiff);

        assertEquals(result, expectedResult);
    }

    @Test
    public void testLiteral() throws Exception {
        JsonNode literalJson = mapper.readTree("{}");
        JsonNode input = mapper.readTree("{\"id\":\"test\"}");
        ((ObjectNode) literalJson).put("eventId", "b6ef1hd-25fh-4dh7-b9vd-87688e65de47");
        String ruleString = literalJson.toString();
        ruleString = "`" + ruleString + "`";
        unitUnderTest.runRuleOnEvent(ruleString, input.toString());
    }

    @Test
    public void testJsonNodeEmpty() throws Exception {
        JsonNode literalJson = mapper.readTree("[[],[\"int\",\"3\"]]");
        int actualSize = literalJson.size();
        Iterator<String> fields = literalJson.fieldNames();

        JsonNode literalJson1 = mapper.readTree("[[[], []]]");
        int actualSize1 = literalJson1.size();
        Iterator<String> fields1 = literalJson1.fieldNames();

        JsonNode literalJson2 = mapper.readTree("[[{}]]");
        int actualSize2 = literalJson2.size();
        Iterator<String> fields2 = literalJson2.fieldNames();

        JsonNode literalJson3 = mapper.readTree("[]");
        int actualSize3 = literalJson3.size();
        Iterator<String> fields3 = literalJson3.fieldNames();

        int wait = 0;
    }
}
