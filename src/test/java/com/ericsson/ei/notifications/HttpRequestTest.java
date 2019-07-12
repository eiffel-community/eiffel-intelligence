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
package com.ericsson.ei.notifications;

import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.App;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
@AutoConfigureMockMvc
public class HttpRequestTest {

    private static final String TEST_URL = "http://www.somehot.com/some/someEndpoint?someParam=Value";
    private static final String MEDIA_TYPE = "application/x-www-form-urlencoded";
    private static final MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String encoding = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());

    private RestPostSubscriptionObject subscription;
    private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;
    final ObjectMapper mapper = new ObjectMapper();

    @Mock
    UrlParser urlParser;

    @Mock
    HttpRequestSender httpRequestSender;

    @Mock
    JenkinsCrumb jenkinsCrumb;

    @InjectMocks
    private HttpRequest httpRequest;

    public static void setUpEmbeddedMongo() throws Exception {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
        String port = "" + mongoClient.getAddress().getPort();
        System.setProperty("spring.data.mongodb.port", port);
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
    }

    @Before
    public void beforeTests() throws IOException {
        subscription = new RestPostSubscriptionObject("My_subscription_name");
        subscription.setAuthenticationType("BASIC_AUTH_JENKINS_CSRF")
                    .setUsername("user")
                    .setPassword("pass")
                    .setNotificationMeta(TEST_URL)
                    .setRestPostBodyMediaType(MEDIA_TYPE);

        final JsonNode subscriptionJsonNode = mapper.readValue(
                subscription.getSubscriptionJson().toString(), JsonNode.class);

        httpRequest.setAggregatedObject("")
                   .setSubscriptionJson(subscriptionJsonNode)
                   .setMapNotificationMessage(mapNotificationMessage)
                   .setUrl(TEST_URL);
    }

    @Test
    public void buildTest() throws Exception {
        String crumbString = "{\"crumbRequestField\": \"my_crumb_key\", \"crumb\": \"my_crumb\"}";
        JsonNode crumbNode = mapper.readTree(crumbString);

        when(jenkinsCrumb.fetchJenkinsCrumb(Mockito.any(), Mockito.any())).thenReturn(crumbNode);
        httpRequest.build();
    }

}
