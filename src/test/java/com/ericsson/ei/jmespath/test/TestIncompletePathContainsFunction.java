package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestIncompletePathContainsFunction {

    private JmesPathInterface unitUnderTest = new JmesPathInterface();
    private static final String inputDiffpath = "src/test/resources/DiffFunctionInput.json";
    private static String jsonInput = "";

    @BeforeClass
    public static void beforeClass() throws Exception {
        jsonInput = FileUtils.readFileToString(new File(inputDiffpath), "UTF-8");
    }

    @Test
    public void testIncompletePathContainsFunctionFails() throws Exception {
        JsonNode expectedResult = JsonNodeFactory.instance.booleanNode(false);
        String processRule = "incomplete_path_contains(@, 'testCase.StartedTime','3.56')";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIncompletePathContainsFunctionNotFirstMember() throws Exception {
        JsonNode expectedResult = JsonNodeFactory.instance.booleanNode(true);
        String processRule = "incomplete_path_contains(@, 'testCaseStartedTime','3.56')";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIncompletePathContainsFunctionFirstMember() throws Exception {
        JsonNode expectedResult = JsonNodeFactory.instance.booleanNode(true);
        String processRule = "incomplete_path_contains(@, 'testCaseExecutions','3.56')";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIncompletePathContainsFunctionSeveralMembers() throws Exception {
        JsonNode expectedResult = JsonNodeFactory.instance.booleanNode(true);
        String processRule = "incomplete_path_contains(@, 'fileInformation.extension','jar')";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(expectedResult, result);
    }
}
