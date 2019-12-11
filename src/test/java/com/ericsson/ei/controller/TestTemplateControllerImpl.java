/*
   Copyright 2018 Ericsson AB.
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
package com.ericsson.ei.controller;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestTemplatesControllerImpl",
        "failed.notifications.collection.name: TestTemplatesControllerImpl-failedNotifications",
        "rabbitmq.exchange.name: TestTemplatesControllerImpl-exchange",
        "rabbitmq.consumer.name: TestTemplatesControllerImpl" })
@ContextConfiguration(
        classes = App.class,
        loader = SpringBootContextLoader.class,
        initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class TestTemplateControllerImpl extends ControllerTestBaseClass {

    private static final String TEMPLATES_EVENTS = "/templates/events";
    private static final String TEMPLATES_RULES = "/templates/rules";
    private static final String TEMPLATES_SUBSCRIPTIONS = "/templates/subscriptions";

    @Test
    public void testGetDownload() throws Throwable {
        JSONObject responseBody = new JSONObject();
        responseBody.put("subscriptions", TEMPLATES_SUBSCRIPTIONS);
        responseBody.put("rules", TEMPLATES_RULES);
        responseBody.put("events", TEMPLATES_EVENTS);
        assertExpectedResponse("/templates", responseBody.toString());
    }

    @Test
    public void testGetSubscriptionsTemplate() throws Throwable {
        assertOkResponseStatus(TEMPLATES_SUBSCRIPTIONS);
    }

    @Test
    public void testGetRulesTemplate() throws Throwable {
        assertOkResponseStatus(TEMPLATES_RULES);
    }

    @Test
    public void testGetEventsTemplate() throws Throwable {
        assertOkResponseStatus(TEMPLATES_EVENTS);
    }

}