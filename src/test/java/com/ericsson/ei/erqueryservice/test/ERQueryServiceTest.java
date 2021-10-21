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
package com.ericsson.ei.erqueryservice.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.exception.PropertyNotFoundException;
import com.ericsson.ei.utils.TestContextInitializer;
import com.ericsson.eiffelcommons.utils.HttpExecutor;
import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;
import com.ericsson.eiffelcommons.utils.ResponseEntity;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


@TestPropertySource(properties = {
        "spring.data.mongodb.database: ERQueryServiceTest",
        "failed.notifications.collection.name: ERQueryServiceTest-failedNotifications",
        "rabbitmq.exchange.name: ERQueryServiceTest-exchange",
        "rabbitmq.queue.suffix: ERQueryServiceTest",
        "event.repository.url: http://localhost:8080/eventrepository/search/" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class ERQueryServiceTest extends Mockito {

    @Autowired
    private ERQueryService erQueryService;
    private HttpExecutor httpExecutor;

    private String eventId = "01";
    private SearchOption searchOption = SearchOption.UP_STREAM;
    private int limitParam = 85;
    private int levels = 2;
    private boolean isTree = true;
    private boolean shallow = true;
    private HttpRequest httpRequest;

    @Before
    public void setUp() throws Exception {
        httpExecutor = mock(HttpExecutor.class);
        httpRequest = new HttpRequest(HttpMethod.POST, httpExecutor);
    }

    @Test(expected = Test.None.class)
    public void testErQueryUpstream() throws Exception {
        BDDMockito.given(httpExecutor.executeRequest(any(HttpRequestBase.class))).willAnswer(validateRequest(Mockito.any(HttpRequestBase.class)));
        erQueryService.sendRequestToER(eventId, searchOption, limitParam, levels, isTree, httpRequest);
    }

    private Answer<ResponseEntity> validateRequest(HttpRequestBase httpRequestBase ) {
        return invocation -> {
            DefaultHttpResponseFactory responseFactory = new DefaultHttpResponseFactory();
            HttpResponse response = responseFactory.newHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 400, ""), null);
            HttpPost postRequest = invocation.getArgument(0);
            String expectedUri = buildUri();
            assertEquals(expectedUri, postRequest.getURI().toString());
            HttpEntity httpEntity = postRequest.getEntity();
            InputStream bodyInputStream = httpEntity.getContent();
            String bodyString = CharStreams.toString(new InputStreamReader(bodyInputStream, Charsets.UTF_8));
            assertEquals("{\"dlt\":[],\"ult\":[\"ALL\"]}", bodyString);
            return new ResponseEntity(response);
        };
    }

    public String buildUri() {
        String uri = String.format("%s%s?limit=%s&tree=%s&shallow=%s&levels=%s",
                erQueryService.getEventRepositoryUrl().trim(), eventId, limitParam, isTree, shallow, levels) ;
        return uri;
    }
}