/*
   Copyright 2019 Ericsson AB.
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
package com.ericsson.ei.notifications;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;

import org.bson.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.TestContextInitializer;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ListDatabasesIterable;
//import com.mongodb.MongoClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: HttpRequestTest",
        "failed.notifications.collection.name: HttpRequestTest-failedNotifications",
        "rabbitmq.exchange.name: HttpRequestTest-exchange",
        "rabbitmq.queue.suffix: HttpRequestTest" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class HttpRequestTest {

    private static final String TEST_URL = "http://www.somehot.com/some/someEndpoint?someParam=Value";
    private static final String MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String encoding;

    private RestPostSubscriptionObject subscription;
    private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    UrlParser urlParser;

    @Mock
    HttpRequestSender httpRequestSender;

    @Mock
    JenkinsCrumb jenkinsCrumb;

    @InjectMocks
    private HttpRequest httpRequest;

    public static void setUpEmbeddedMongo() throws Exception {
        //testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        //mongoClient = testsFactory.newMongo();
    	ListDatabasesIterable<Document> list = mongoClient.listDatabases();
        MongoCursor<Document> iter = list.iterator(); 
        /*while (iter.hasNext()) {
            iter.getServerAddress();
        }*/
        //String port = "" + mongoClient.getAddress().getPort();
        String port = "" + iter.getServerAddress().getPort();
        System.setProperty("spring.data.mongodb.port", port);
    }

    @BeforeClass
    public static void init() throws Exception {
        //setUpEmbeddedMongo();
    }

    @Before
    public void beforeTests() throws IOException {
        subscription = new RestPostSubscriptionObject("My_subscription_name");
        subscription.setAuthenticationType("BASIC_AUTH_JENKINS_CSRF")
                    .setUsername(USERNAME)
                    .setPassword(PASSWORD)
                    .setNotificationMeta(TEST_URL)
                    .setRestPostBodyMediaType(MEDIA_TYPE);

        final JsonNode subscriptionJsonNode = mapper.readValue(
                subscription.getSubscriptionJson().toString(), JsonNode.class);

        httpRequest.setAggregatedObject("")
                   .setSubscriptionJson(subscriptionJsonNode)
                   .setMapNotificationMessage(mapNotificationMessage)
                   .setUrl(TEST_URL);

        encoding = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
    }

    /**
     * Test the complete process by giving a crumb key, and a crumb value an ensure those are added
     * correctly to the headers, this should confirm that all steps in the headers preparation is
     * completed.
     *
     * @throws Exception
     */
    @Test
    public void testAuthentication() throws Exception {
        String crumb = "my_crumb";
        String crumbKey = "my_crumb_key";
        String crumbResponse = "{\"crumbRequestField\": \"" + crumbKey + "\", \"crumb\": \"" + crumb
                + "\"}";
        JsonNode crumbResponseNode = mapper.readTree(crumbResponse);

        // Here we ensure encoding is correct, and URL is correct or crumb will become null.
        when(jenkinsCrumb.fetchJenkinsCrumb(encoding, TEST_URL)).thenReturn(
                crumbResponseNode);

        // Build the request.
        httpRequest.build();

        // crom the request extract the processed crumb and remove '[]'
        String processedCrumb = httpRequest.getRequest()
                                           .getHeaders()
                                           .get(crumbKey)
                                           .toString()
                                           .replace("[", "")
                                           .replace("]", "");

        assertEquals("The expected crumb vs the crumb in headers: ", crumb, processedCrumb);
    }

    /**
     * Test to ensure that the correct content type is set in the request.
     *
     * @throws Exception
     */
    @Test
    public void testCorrectMediaTypeInRequest() throws Exception {
        // Build the request.
        httpRequest.build();

        String contentType = httpRequest.getRequest().getHeaders().getContentType().toString();
        assertEquals("Content type is equal to content type in subscription", MEDIA_TYPE,
                contentType);
    }

}
