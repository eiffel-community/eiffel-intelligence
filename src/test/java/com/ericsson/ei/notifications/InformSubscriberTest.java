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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.powermock.reflect.Whitebox.invokeMethod;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.notifications.HttpRequest.HttpRequestFactory;
import com.ericsson.ei.subscription.RunSubscription;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(MockitoJUnitRunner.class)
public class InformSubscriberTest {

    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String aggregatedInternalPath = "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";
    private static final String aggregatedPathForMapNotification = "src/test/resources/aggregatedObjectForMapNotification.json";
    private static final String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static final String artifactRequirementSubscriptionPath = "src/test/resources/artifactRequirementSubscription.json";
    private static final String subscriptionPathForAuthorization = "src/test/resources/SubscriptionObjectForAuthorization.json";
    private static final String subscriptionRepeatFlagTruePath = "src/test/resources/SubscriptionRepeatFlagTrueObject.json";
    private static final String subscriptionForMapNotificationPath = "src/test/resources/subscriptionForMapNotification.json";
    private static final String regex = "^\"|\"$";

    private HttpHeaders headersWithAuth = new HttpHeaders();
    private HttpHeaders headersWithoutAuth = new HttpHeaders();
    private String aggregatedObject;
    private String aggregatedInternalObject;
    private String aggregatedObjectMapNotification;
    private String subscriptionData;
    private String artifactRequirementSubscriptionData;
    private String subscriptionDataForAuthorization;
    private String url;
    private String urlAuthorization;
    private String subscriptionRepeatFlagTrueData;
    private String subscriptionForMapNotification;
    private MongodForTestsFactory testsFactory;
    private MongoClient mongoClient = null;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    MongoDBHandler mongoDBHandler;

    @Mock
    JmesPathInterface jmespath;

    @Mock
    HttpRequest httpRequest;

    @Mock
    HttpRequestFactory httpRequestFactory;

    @InjectMocks
    private InformSubscriber informSubscriber;
    private JsonNode subscriptionNode;

    @Before
    public void beforeTests() throws IOException {
        when(jmespath.runRuleOnEvent(any(), any())).thenReturn(
                mapper.readValue("\"mock_value\"", JsonNode.class));
        when(httpRequestFactory.createHttpRequest()).thenReturn(httpRequest);
        when(httpRequest.setAggregatedObject(any())).thenReturn(httpRequest);
        when(httpRequest.setMapNotificationMessage(any())).thenReturn(httpRequest);
        when(httpRequest.setSubscriptionJson(any())).thenReturn(httpRequest);
        when(httpRequest.setUrl(any())).thenReturn(httpRequest);

        subscriptionData = FileUtils.readFileToString(new File(subscriptionPath), "UTF-8");
        subscriptionNode = mapper.readTree(subscriptionData);
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");

        //////////////////////////////

//        aggregatedInternalObject = FileUtils.readFileToString(new File(aggregatedInternalPath),
//                "UTF-8");
//        aggregatedObjectMapNotification = FileUtils.readFileToString(
//                new File(aggregatedPathForMapNotification),
//                "UTF-8");
//
//        artifactRequirementSubscriptionData = FileUtils.readFileToString(
//                new File(artifactRequirementSubscriptionPath),
//                "UTF-8");
//        subscriptionRepeatFlagTrueData = FileUtils.readFileToString(
//                new File(subscriptionRepeatFlagTruePath), "UTF-8");
//
//        subscriptionForMapNotification = FileUtils.readFileToString(
//                new File(subscriptionForMapNotificationPath),
//                "UTF-8");
//
//        url = new JSONObject(subscriptionData).getString("notificationMeta")
//                                              .replaceAll(regex, "")
//                                              .replaceAll("'", "");

    }

    @Test
    public void testRestPostTrigger() throws Exception {
        when(httpRequest.perform()).thenReturn(true);
        informSubscriber.informSubscriber(aggregatedObject, subscriptionNode);
        verify(httpRequest, times(1)).perform();
    }

    @Test
    public void testRestPostTriggerFailure() throws Exception {
        when(httpRequest.perform()).thenReturn(false);
        // Setting failAttempts to 3
        informSubscriber.setFailAttempt(3);
        informSubscriber.informSubscriber(aggregatedObject, subscriptionNode);
        // Should expect 4 tries to perform a HTTP request
        verify(httpRequest, times(4)).perform();
        // Should try to save missed notification to DB
        verify(mongoDBHandler, times(1)).insertDocument(any(), any(), any());
    }

    @Test
    public void testRestPostTriggerThrowsAuthenticationException() throws Exception {
        when(httpRequest.perform()).thenThrow(new AuthenticationException(""));
        // Setting failAttempts to 3
        informSubscriber.setFailAttempt(3);
        informSubscriber.informSubscriber(aggregatedObject, subscriptionNode);
        // Should expect 1 tries to perform a HTTP request since AuthenticationException should
        // ignore failAttempt.
        verify(httpRequest, times(1)).perform();
        // Should try to save missed notification to DB
        verify(mongoDBHandler, times(1)).insertDocument(any(), any(), any());
    }

//    @Test
//    public void testMapNotificationMessage() throws Exception {

//
//    String jmesPathRule = "fileInformation[?extension=='jar'] | [0]";
//    String urlWithoutJmespath = "http://127.0.0.1:3000/ei/buildParam?token='test_token'&json=";
//    String urlWithJmesPath = urlWithoutJmespath + jmesPathRule;
//        MultiValueMap<String, String> actual = invokeMethod(informSubscriber, "mapNotificationMessage",
//                aggregatedObjectMapNotification, mapper.readTree(subscriptionForMapNotification));
//        MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
//        expected.add("", "{\"conclusion\":\"SUCCESSFUL\",\"id\":\"TC5\"}");
//        assertEquals(expected, actual);
////      String expectedExtraction = jmespath.runRuleOnEvent(jmesPathRule, aggregatedObject).toString();
//
//    }

    private MultiValueMap<String, String> mapNotificationMessage(String data) throws Exception {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();

        ArrayNode arrNode = (ArrayNode) mapper.readTree(data).get("notificationMessageKeyValues");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (!objNode.get("formkey")
                            .toString()
                            .replaceAll(regex, "")
                            .equals("Authorization")) {

                    mapNotificationMessage.add(objNode.get("formkey")
                                                      .toString()
                                                      .replaceAll(regex, ""),
                            jmespath
                                    .runRuleOnEvent(objNode.get("formvalue")
                                                           .toString()
                                                           .replaceAll(regex, ""),
                                            aggregatedObject)
                                    .toString()
                                    .replaceAll(regex, ""));
                }
            }
        }
        return mapNotificationMessage;
    }
//
//    @Test
//    public void testPrepareMissedNotification() {
//        // TODO: test
//    }
//
//    private static ResponseEntity<String> createResponseEntity(String body, HttpStatus httpStatus) {
//        return new ResponseEntity<String>(body, new HttpHeaders(), httpStatus);
//    }
}
