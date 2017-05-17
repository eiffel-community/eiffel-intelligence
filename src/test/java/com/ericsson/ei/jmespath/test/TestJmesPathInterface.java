package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJmesPathInterface {
    private JmesPathInterface unitUnderTest;

    static Logger log = (Logger) LoggerFactory.getLogger(TestJmesPathInterface.class);

    @Test
    public void testRunRuleOnEvent() {
        unitUnderTest = new JmesPathInterface();
        String jsonInput = null;
        String jsonOutput = null;
        JsonNode output = null;
        try {
            jsonInput = FileUtils.readFileToString(new File("src/test/resources/input.json"));
            jsonOutput = FileUtils.readFileToString(new File("src/test/resources/output.json"));
            ObjectMapper objectmapper = new ObjectMapper();
            output = objectmapper.readTree(jsonOutput);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String ExtractionRules_test = "{ id : meta.id, type : meta.type, time : meta.time, gav : data.gav, fileInformation : data.fileInformation, buildCommand : data.buildCommand }";
        JsonNode result = unitUnderTest.runRuleOnEvent(ExtractionRules_test, jsonInput);
        assertEquals(result, output);
    }

}
