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
import com.ericsson.ei.rules.TestRulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestExtractionHandler {
    private ExtractionHandler extractionHandler;

    private final String outputFilePath = "src/test/resources/ExtractedContent.json";
    private final String rulesFilePath = "src/test/resources/RulesHandlerOutput2.json";
    private final String eventFilePath = "src/test/resources/EiffelArtifactCreatedEvent.json";

    private JsonNode result;
    private JsonNode expectedOutput;
    private RulesObject rulesObject;
    private String event;

    static Logger log = LoggerFactory.getLogger(TestRulesHandler.class);

    @Test
    public void testExtractContent() {
        try {
            String outputFileContents = FileUtils.readFileToString(new File(outputFilePath), "UTF-8");
            String rulesFileContents = FileUtils.readFileToString(new File(rulesFilePath), "UTF-8");
            event = FileUtils.readFileToString(new File(eventFilePath), "UTF-8");

            ObjectMapper objectmapper = new ObjectMapper();
            expectedOutput = objectmapper.readTree(outputFileContents);
            rulesObject = new RulesObject(objectmapper.readTree(rulesFileContents));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        extractionHandler = new ExtractionHandler();
        JmesPathInterface jmesPathInterface = new JmesPathInterface();
        extractionHandler.setJmesPathInterface(jmesPathInterface);
        try {
            Method method = extractionHandler.getClass().getDeclaredMethod("extractContent",
                    new Class[] { RulesObject.class, String.class });
            method.setAccessible(true);
            result = (JsonNode) method.invoke(extractionHandler, new Object[] { rulesObject, event });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        assertEquals(result, expectedOutput);
    }
}
