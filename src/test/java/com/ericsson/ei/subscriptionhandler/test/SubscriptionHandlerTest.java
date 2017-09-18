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
package com.ericsson.ei.subscriptionhandler.test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.ericsson.ei.App;
import com.ericsson.ei.subscriptionhandler.RunSubscription;
import com.ericsson.ei.subscriptionhandler.SpringRestTemplate;
import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class SubscriptionHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(SpringRestTemplate.class);

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static String aggregatedObject;
    private static String subscriptionData;

    @Autowired
    private SpringRestTemplate springRestTemplate;

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private SubscriptionHandler handler;

    @Before
    public void setUp() {

        springRestTemplate = new SpringRestTemplate(new RestTemplateBuilder());
        try {
            aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath));
            subscriptionData = FileUtils.readFileToString(new File(subscriptionPath));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Test
    public void checkRequirementTypeTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        JsonNode aggregatedJson = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            aggregatedJson = mapper.readTree(aggregatedObject);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        log.info("RequirementNode : " + requirementNode.toString());
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        boolean output = handler.checkRequirementType(requirementIterator, aggregatedObject);
        assertEquals(output, true);
    }

    @Test
    public void runSubscriptionOnObjectTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        JsonNode requirement = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            requirement = subscriptionJson.get("requirements").get(0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        boolean output = runSubscription.runSubscriptionOnObject(aggregatedObject, requirement, subscriptionJson);
        assertEquals(output, true);
    }

}
