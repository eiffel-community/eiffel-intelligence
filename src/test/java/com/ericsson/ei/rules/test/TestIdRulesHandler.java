package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.IdRulesHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestIdRulesHandler {

    private final String rulesPath = "src/test/resources/ArtifactRules_new.json";
    private final String eventPath = "src/test/resources/EiffelArtifactCreatedEvent.json";

    static Logger log = (Logger) LoggerFactory.getLogger(TestRulesHandler.class);

    @Test
    public void testGetIds(){
        RulesObject rulesObject = null;
        JsonNode eventJsonNode = null;
        String eventFile = "";
        String expectedIds = "e90daae3-bf3f-4b0a-b899-67834fd5ebd0";

        IdRulesHandler idRulesHandler = new IdRulesHandler();
        JmesPathInterface jmespath = new JmesPathInterface();
        idRulesHandler.setJmesPathInterface(jmespath);
        try{
            String jsonRules = FileUtils.readFileToString(new File(rulesPath));
            ObjectMapper rulesObjectMapper = new ObjectMapper();

            eventFile = FileUtils.readFileToString(new File(eventPath));

            rulesObject = new RulesObject(rulesObjectMapper.readTree(jsonRules.replace("[", "").replace("]", "")));
            System.out.println("RulesObject: " + rulesObject.getJsonRulesObject());
            System.out.println("EventJson: " + eventJsonNode.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        JsonNode ids = idRulesHandler.getIds(rulesObject, eventFile);
        System.out.println("Ids: " + ids.textValue());

        assertEquals(expectedIds, ids.textValue());
    }
}
