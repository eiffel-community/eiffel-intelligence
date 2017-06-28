package com.ericsson.ei.extraction.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.ExtractionHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.rules.test.TestRulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestExtractionHandler {
    private ExtractionHandler classUnderTest;

    private final String outputFilePath = "src/test/resources/ExtractedContent.json";
    private final String rulesFilePath = "src/test/resources/RulesHandlerOutput2.json";
    private final String eventFilePath = "src/test/resources/EiffelArtifactCreatedEvent.json";

    private JsonNode result;
    private JsonNode expectedOutput;
    private RulesObject rulesObject;
    private String event;

    static Logger log = (Logger) LoggerFactory.getLogger(TestRulesHandler.class);

    /*@Mock MergeHandler mergeHandler;
    @Mock ProcessRulesHandler processRulesHandler;
    @Mock HistoryIdRulesHandler historyIdRulesHandler;*/

    @Test
    public void testExtractContent() {
        try {
            String outputFileContents = FileUtils.readFileToString(new File(outputFilePath));
            String rulesFileContents = FileUtils.readFileToString(new File(rulesFilePath));
            event = FileUtils.readFileToString(new File(eventFilePath));

            ObjectMapper objectmapper = new ObjectMapper();
            expectedOutput = objectmapper.readTree(outputFileContents);
            rulesObject = new RulesObject(objectmapper.readTree(rulesFileContents));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        classUnderTest = new ExtractionHandler();
        JmesPathInterface jmesPathInterface = new JmesPathInterface();
        classUnderTest.setJmesPathInterface(jmesPathInterface);

        // Execute private method extractContent in isolation
        try {
            Method method = classUnderTest.getClass().getDeclaredMethod("extractContent", new Class[] {RulesObject.class, String.class});
            method.setAccessible(true);
            result = (JsonNode)method.invoke(classUnderTest, new Object[] {rulesObject, event});
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        assertEquals(result, expectedOutput);
    }
}
