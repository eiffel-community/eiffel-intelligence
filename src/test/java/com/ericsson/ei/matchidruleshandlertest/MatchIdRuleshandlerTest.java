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
            jsonInput = FileUtils.readFileToString(new File(inputFilePath));
            jsonOutput = FileUtils.readFileToString(new File(outputFilePath));
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
