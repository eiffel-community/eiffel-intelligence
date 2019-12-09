package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jmespath.SplitFunction;
import com.fasterxml.jackson.databind.JsonNode;

public class TestStringSplittingFunction {
    private JmesPathInterface unitUnderTest = new JmesPathInterface();
    private SplitFunction objectToTest = new SplitFunction();
    private static final String purl = "pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0";

    private static final String inputDiffpath = "src/test/resources/DiffFunctionInput.json";
    private static String jsonInput = "";

    @BeforeClass
    public static void beforeClass() throws Exception {
        jsonInput = FileUtils.readFileToString(new File(inputDiffpath), "UTF-8");
    }

    @Test
    public void testStringSplit() throws Exception {
        String expectedResult[] = { "pkg:maven", "com.mycompany.myproduct", "artifact-name@1.0.0" };
        String separator = "/";
        String actualResult[] = objectToTest.split(purl, separator);
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
        assertEquals(expectedResult[2], actualResult[2]);
    }

    @Test
    public void testStringSplitFunction() throws Exception {
        // String processRule = "split(data.identity, '/')";

        String processRule = "split(data.identity, '@|/')";

        String expectedResult = "com.mycompany.myproduct";
        JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
        JsonNode actualResult = result.get(2);
        assertEquals(expectedResult, actualResult.textValue());
    }
}
