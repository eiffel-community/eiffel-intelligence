/*
   Copyright 2018 Ericsson AB.
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestIncompleteJmesPathFilter {
    private JmesPathInterface unitUnderTest = new JmesPathInterface();;
    private static final String aggregatedObjectFilePath = "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";

    private static String jsonInput = "";
    private static ObjectMapper mapper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        jsonInput = FileUtils.readFileToString(new File(aggregatedObjectFilePath), "UTF-8");
        mapper = new ObjectMapper();
    }

    @Test
    public void testFilterObjectWithWholePath() throws Exception {
        JsonNode expectedResult = mapper.readTree("{\"eventId\":\"33d05e6f-9bd9-4138-83b6-e20cc74680a3\"}");
        String processRule = "{eventId : incomplete_path_filter(@, 'aggregatedObject.publications[0].eventId')}";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(result, expectedResult);
    }

    @Test
    public void testFilterObjectWithPartialPath() throws Exception {
        String expectedResult = "{\"eventId\":\"[33d05e6f-9bd9-4138-83b6-e20cc74680a3, 33d05e6f-9bd9-4138-83b6-e20cc74681b5]\"}";
        String processRule = "{eventId : incomplete_path_filter(@, 'publications.eventId')}";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(result.toString(), expectedResult);
    }

    @Test
    public void testFilterObjectWithKey() throws Exception {
        String expectedResult = "\"[1481875921843, 1481875988767, 1481875921763, 1481875944272, 5005, 1481875891763, 2000]\"";
        String processRule = "incomplete_path_filter(@, 'time')";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(result.toString(), expectedResult);
    }

    @Test
    public void testFilterObjectWithManyKeys() throws Exception {
        String expectedResult = "{\"time\":\"[1481875921843, 1481875988767, 1481875921763, 1481875944272, 5005, 1481875891763, 2000]\",\"svnIdentifier\":\"null\"}";
        String processRule = "{time : incomplete_path_filter(@, 'time'), svnIdentifier : incomplete_path_filter(@, 'svnIdentifier')}";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(result.toString(), expectedResult);
    }

    @Test
    public void testFilterObjectAndReturnValueGroups() throws Exception {
        String expectedResult = "{\"gav\":\"[{\\\"groupId\\\":\\\"com.mycompany.myproduct\\\",\\\"artifactId\\\":\\\"sub-system\\\",\\\"version\\\":\\\"1.1.0\\\"}, "
                + "{\\\"groupId\\\":\\\"com.internalcompany.internalproduct\\\",\\\"artifactId\\\":\\\"internal-system\\\",\\\"version\\\":\\\"1.99.0\\\"}, "
                + "{\\\"groupId\\\":\\\"com.othercompany.otherproduct\\\",\\\"artifactId\\\":\\\"other-system\\\",\\\"version\\\":\\\"1.33.0\\\"}, "
                + "{\\\"groupId\\\":\\\"com.othercompany.secondproduct\\\",\\\"artifactId\\\":\\\"other-system\\\",\\\"version\\\":\\\"1.33.0\\\"}]\"}";
        String processRule = "{gav : incomplete_path_filter(@, 'gav')}";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(result.toString(), expectedResult);
    }
}
