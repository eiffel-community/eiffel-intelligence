package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRulesHandler {
    private RulesHandler unitUnderTest;
    private final String inputFilePath = "src/test/resources/EiffelSourceChangeCreatedEvent.json";
    private final String outputFilePath = "src/test/resources/RulesHandlerOutput.json";
    private final String rulesPath = "src/test/resources/ArtifactRules_new.json";


    static Logger log = (Logger) LoggerFactory.getLogger(TestRulesHandler.class);

    @Before public void setUp() throws Exception {
        unitUnderTest = new RulesHandler();
        unitUnderTest.setRulePath(rulesPath);
        unitUnderTest.init();
    }

    @Test
    public void testPrintJson() {
        String jsonInput = null;
        String jsonOutput = null;
        RulesObject result = null;
        RulesObject output = null;
        try {
            jsonInput = FileUtils.readFileToString(new File(inputFilePath));
            jsonOutput = FileUtils.readFileToString(new File(outputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            output = new RulesObject(objectmapper.readTree(jsonOutput));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        result = unitUnderTest.getRulesForEvent(jsonInput);

        assertEquals(result, output);
    }

}
