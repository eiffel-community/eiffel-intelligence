/*
    Copyright 2025 Ericsson AB.
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
package com.ericsson.ei.erqueryservice.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.erservice.OpenApiErApplication;
import com.ericsson.eiffelcommons.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests ERQueryService against a real OpenAPI-based Event Repository service.
 * Does not require MongoDB or RabbitMQ — only the ER HTTP service.
 */
public class ERQueryServiceWithOpenAPITest {

    static final int ER_PORT = 8764;
    static final String ER_URL = "http://localhost:" + ER_PORT + "/search/";

    private static ConfigurableApplicationContext erContext;
    private static ERQueryService erQueryService;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setUp() {
        // Start the OpenAPI ER service
        SpringApplication erApp = new SpringApplicationBuilder(OpenApiErApplication.class).build();
        Properties props = new Properties();
        props.put("server.port", String.valueOf(ER_PORT));
        props.put("er.security.permitAll", "true");
        props.put("spring.autoconfigure.exclude",
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,"
                + "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration");
        ConfigurableEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new PropertiesPropertySource("erProps", props));
        erApp.setEnvironment(env);
        erContext = erApp.run();

        // Configure ERQueryService to point to the OpenAPI ER
        erQueryService = new ERQueryService();
        ReflectionTestUtils.setField(erQueryService, "eventRepositoryUrl", ER_URL);
        ReflectionTestUtils.setField(erQueryService, "shallow", true);
    }

    @AfterClass
    public static void tearDown() {
        if (erContext != null) {
            erContext.close();
        }
    }

    @Test
    public void testUpstreamSearchReturnsTree() throws Exception {
        // CompositionDefined-1 has ELEMENT links to two ArtifactCreated events
        String eventId = "ab6ef12d-25fb-4d77-b9fd-87688e66de47";
        ResponseEntity response = erQueryService.getEventStreamDataById(
                eventId, SearchOption.UP_STREAM, -1, -1, true);

        assertNotNull("Response should not be null", response);
        assertEquals(200, response.getStatusCode());

        JsonNode result = mapper.readTree(response.getBody());
        JsonNode upstream = result.get("upstreamLinkObjects");
        assertNotNull("upstreamLinkObjects should be present", upstream);
        assertTrue("upstreamLinkObjects should be an array", upstream.isArray());
        assertTrue("upstream tree should contain the start event plus subtrees", upstream.size() > 1);
    }

    @Test
    public void testDownstreamSearchReturnsTree() throws Exception {
        // ArtifactCreated-1 is referenced by CompositionDefined-1 via ELEMENT link
        String eventId = "a100572b-c3j4-441e-abc9-b62f48080011";
        ResponseEntity response = erQueryService.getEventStreamDataById(
                eventId, SearchOption.DOWN_STREAM, -1, -1, true);

        assertNotNull("Response should not be null", response);
        assertEquals(200, response.getStatusCode());

        JsonNode result = mapper.readTree(response.getBody());
        JsonNode downstream = result.get("downstreamLinkObjects");
        assertNotNull("downstreamLinkObjects should be present", downstream);
        assertTrue("downstreamLinkObjects should be an array", downstream.isArray());
    }

    @Test
    public void testUpAndDownStreamSearch() throws Exception {
        String eventId = "ab6ef12d-25fb-4d77-b9fd-87688e66de47";
        ResponseEntity response = erQueryService.getEventStreamDataById(
                eventId, SearchOption.UP_AND_DOWN_STREAM, -1, -1, true);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        JsonNode result = mapper.readTree(response.getBody());
        assertNotNull(result.get("upstreamLinkObjects"));
        assertNotNull(result.get("downstreamLinkObjects"));
    }

    @Test
    public void testSearchWithLimitedLevels() throws Exception {
        String eventId = "ab6ef12d-25fb-4d77-b9fd-87688e66de47";
        ResponseEntity response = erQueryService.getEventStreamDataById(
                eventId, SearchOption.UP_STREAM, -1, 1, true);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        JsonNode result = mapper.readTree(response.getBody());
        assertNotNull(result.get("upstreamLinkObjects"));
    }

    @Test
    public void testSearchForNonExistentEventReturns404() throws Exception {
        String eventId = "non-existent-id";
        ResponseEntity response = erQueryService.getEventStreamDataById(
                eventId, SearchOption.UP_STREAM, -1, -1, true);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }
}
