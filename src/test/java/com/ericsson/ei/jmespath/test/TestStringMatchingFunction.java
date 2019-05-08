package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jmespath.MatchFunction;
import com.fasterxml.jackson.databind.JsonNode;

public class TestStringMatchingFunction {
    private JmesPathInterface unitUnderTest = new JmesPathInterface();
    private MatchFunction objectToTest = new MatchFunction();
    private static final String testString_1 = "pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0";
    private static final String testString_2 = "pkg:maven/myproduct/artifact-name@1.0.0";
    private static final String testPattern = "[a-z]+\\.[a-z.]+[a-z]";

    private static final String inputDiffpath = "src/test/resources/DiffFunctionInput.json";
    private static String jsonInput = "";

    @BeforeClass
    public static void beforeClass() throws Exception {
        jsonInput = FileUtils.readFileToString(new File(inputDiffpath), "UTF-8");
    }

    @Test
    public void testStringExtractor() throws Exception {
        String expectedResult_1 = "com.mycompany.myproduct";
        String actualResult_1 = objectToTest.match(testString_1, testPattern);
        String expectedResult_2 = "";
        String actualResult_2 = objectToTest.match(testString_2, testPattern);
        assertEquals(expectedResult_1, actualResult_1);
        assertEquals(expectedResult_2, actualResult_2);
    }

    @Test
    public void testStringSplitFunction() throws Exception {
        String processRule = "match(data.identity, '[a-z]+\\\\.[a-z.]+[a-z]')";
        String expectedResult = "com.mycompany.myproduct";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        assertEquals(expectedResult, result.textValue());
    }

}
