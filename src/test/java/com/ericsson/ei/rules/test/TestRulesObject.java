package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRulesObject {
    private RulesObject unitUnderTest;
    private final String inputFilePath = "src/test/resources/RulesHandlerOutput2.json";
    private JsonNode rulesJson;

    static Logger log = (Logger) LoggerFactory.getLogger(TestRulesObject.class);

    @Test
    public void testPrintJson() {
        String expectedOutput = "{ id : meta.id, type : meta.type, time : meta.time, gav : data.gav, fileInformation " +
                ": data.fileInformation, buildCommand : data.buildCommand }";

        String result;

        try {
            String rulesString = FileUtils.readFileToString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            rulesJson = objectmapper.readTree(rulesString);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        unitUnderTest = new RulesObject(rulesJson);

        result = unitUnderTest.getExtractionRules();

        assertEquals(result, expectedOutput);
    }

}
