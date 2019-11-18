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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.invokeMethod;

import java.io.File;
import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.exception.NotificationFailureException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.notifications.HttpRequest.HttpRequestFactory;
import com.ericsson.eiffelcommons.subscriptionobject.MailSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class InformSubscriberTest {

    private static final String MOCKED_JMESPATH_RETURN_VALUE = "{\"conclusion\":\"SUCCESSFUL\",\"id\":\"TC5\"}";
    private static final int REST_POST_RETRIES = 4;
    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";

    private String aggregatedObject;
    private RestPostSubscriptionObject restPostSubscriptionObject;
    private JsonNode restPostSubscriptionNode;
    private MailSubscriptionObject mailSubscriptionObject;
    private JsonNode mailSubscriptionNode;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    MongoDBHandler mongoDBHandler;

    @Mock
    JmesPathInterface jmespath;

    @Mock
    HttpRequest httpRequest;

    @Mock
    MimeMessage mimeMessage;

    @Mock
    HttpRequestFactory httpRequestFactory;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    private InformSubscriber informSubscriber;

    @Before
    public void beforeTests() throws IOException {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
    }

    /**
     * In a successfull rest post call there should be no retries.
     *
     * @throws Exception
     */
    @Test
    public void testRestPostTrigger() throws Exception {
        beforeRestPostTests();
        informSubscriber.informSubscriber(aggregatedObject, restPostSubscriptionNode);
        verify(httpRequest, times(1)).perform();
    }

    /**
     * When failing to perfom a rest post call we should do retries, and if none succeed it should
     * be saved in the failed notification collection.
     *
     * @throws Exception
     */
    @Test
    public void testRestPostTriggerFailure() throws Exception {
        beforeRestPostTests();
        doThrow(new Exception("")).when(httpRequest).perform();

        informSubscriber.informSubscriber(aggregatedObject, restPostSubscriptionNode);
        // Should expect 4 tries to perform a HTTP request
        verify(httpRequest, times(REST_POST_RETRIES + 1)).perform();
        // Should try to save failed notification to DB
        verify(mongoDBHandler, times(1)).insertDocument(any(), any(), any());
    }

    /**
     * When an AuthenticationException is thrown we should never retry the connection even if retry
     * is set to higher then 0. Failed notification should be saved in DB.
     *
     * @throws Exception
     */
    @Test
    public void testRestPostTriggerThrowsAuthenticationException() throws Exception {
        beforeRestPostTests();
        doThrow(new AuthenticationException("")).when(httpRequest).perform();

        informSubscriber.informSubscriber(aggregatedObject, restPostSubscriptionNode);
        // Should expect 1 tries to perform a HTTP request since AuthenticationException should
        // ignore failAttempt.
        verify(httpRequest, times(1)).perform();
        // Should try to save failed notification to DB
        verify(mongoDBHandler, times(1)).insertDocument(any(), any(), any());
    }

    /**
     * If sedning email is successfull then there should be no tries to save massed notification to
     * DB.
     *
     * @throws Exception
     */
    @Test
    public void testMailTrigger() throws Exception {
        beforeEmailTests();
        informSubscriber.informSubscriber(aggregatedObject, mailSubscriptionNode);
        verify(emailSender, times(1)).sendEmail(mimeMessage);
        verify(mongoDBHandler, never()).insertDocument(any(), any(), any());
    }

    /**
     * When EmailSender throws a NotificationFailureException ensure that this is saved in the DB.
     *
     * @throws Exception
     */
    @Test
    public void testMailTriggerThrowsException() throws Exception {
        beforeEmailTests();
        doThrow(new NotificationFailureException("")).when(emailSender).sendEmail(mimeMessage);
        informSubscriber.informSubscriber(aggregatedObject, mailSubscriptionNode);
        // Should try to save failed notification to DB
        verify(mongoDBHandler, times(1)).insertDocument(any(), any(), any());
    }

    /**
     * Test that MapNotificationMessage correctly extracts values from subscription and sends them
     * to jmespath and correctly creates a MultiValueMap with the returned data.
     *
     * @throws Exception
     */
    @Test
    public void testMapNotificationMessage() throws Exception {
        String formValue = "conclusion:testCaseExecutions";

        RestPostSubscriptionObject subscriptionObject = new RestPostSubscriptionObject(
                "Notification_Message");
        subscriptionObject.addNotificationMessageKeyValue("", formValue);

        when(jmespath.runRuleOnEvent(formValue, aggregatedObject)).thenReturn(
                mapper.readValue(MOCKED_JMESPATH_RETURN_VALUE, JsonNode.class));

        MultiValueMap<String, String> actual = invokeMethod(informSubscriber,
                "mapNotificationMessage", aggregatedObject,
                mapper.readTree(subscriptionObject.toString()));

        MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
        expected.add("", MOCKED_JMESPATH_RETURN_VALUE);

        assertEquals(expected, actual);
    }

    /**
     * Preperation before rest post subscription tests to create rest post subscription and stubb
     * HttpRequest setters.
     *
     * @throws IOException
     */
    private void beforeRestPostTests() throws IOException {
        when(httpRequestFactory.createHttpRequest()).thenReturn(httpRequest);
        when(httpRequest.setAggregatedObject(any())).thenReturn(httpRequest);
        when(httpRequest.setMapNotificationMessage(any())).thenReturn(httpRequest);
        when(httpRequest.setSubscriptionJson(any())).thenReturn(httpRequest);
        when(httpRequest.setUrl(any())).thenReturn(httpRequest);

        restPostSubscriptionObject = new RestPostSubscriptionObject("Name");
        restPostSubscriptionObject.setAuthenticationType("NO_AUTH").setNotificationMeta("some_url");
        restPostSubscriptionNode = mapper.readTree(restPostSubscriptionObject.toString());

        // Setting failAttempts to 3
        informSubscriber.setFailAttempt(REST_POST_RETRIES);
    }

    /**
     * Preperation before email subscription tests to create email subscription and stubb jmespath.
     *
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    private void beforeEmailTests() throws IOException, JsonParseException, JsonMappingException {
        when(jmespath.runRuleOnEvent(any(), any())).thenReturn(
                mapper.readValue("\"mock_value\"", JsonNode.class));
        when(emailSender.prepareEmailMessage(any(), any(), any())).thenReturn(mimeMessage);

        mailSubscriptionObject = new MailSubscriptionObject("Name");
        mailSubscriptionObject.setNotificationMeta("some@email.com")
                              .addNotificationMessageKeyValue("",
                                      "{mydata: [{ fullaggregation : to_string(@) }]}");
        mailSubscriptionNode = mapper.readTree(mailSubscriptionObject.toString());
    }
}
