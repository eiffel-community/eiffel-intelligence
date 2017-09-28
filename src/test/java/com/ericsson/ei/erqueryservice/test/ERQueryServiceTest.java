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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.*;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ERQueryServiceTest {

    @Autowired
    ERQueryService erQueryService;

    @Mock
    RestOperations rest;

    String eventId = "01";
    int searchAction = 0;
    int limitParam = 85;
    int levels = 2;
    boolean isTree = true;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testErQueryUpstream() {
        erQueryService.setRest(rest);
        searchAction = ERQueryService.UPSTREAM;
        given(rest.exchange(Mockito.any(URI.class), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).willAnswer(returnRestExchange(Mockito.any(URI.class), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class)));
        ResponseEntity result = erQueryService.getEventStreamDataById(eventId, searchAction, limitParam, levels, isTree);
    }

    Answer<ResponseEntity> returnRestExchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
            Class responseType) {
        return new Answer<ResponseEntity>() {
            @Override
            public ResponseEntity answer(InvocationOnMock invocation) throws Throwable {
                URI arg0 = invocation.getArgumentAt(0, URI.class);
                String expectedUri = buildUri();
                assertEquals(expectedUri, arg0.toString());
                HttpEntity arg2 = invocation.getArgumentAt(2, HttpEntity.class);
                ObjectNode body = (ObjectNode) arg2.getBody();
                assertBody(body);
                boolean firstStop = true;
                return new ResponseEntity(HttpStatus.OK);
            }
        };
    }

    public String buildUri() {
        String uri = "";
//        example uri
//        http://localhost:8080/search/01?limit=85&levels=2&tree=true
        uri += erQueryService.getUrl().trim() + eventId + "?limit=" + limitParam + "&levels=" + levels + "&tree=" + isTree;
        return uri;
    }

    public void assertBody(ObjectNode body) {
//    	example body
//      {"ult":["ALL"]}
        boolean bodyNotNull = body != null;
        assertEquals(bodyNotNull, true);
        boolean searchActionIsRight = false;
        if (searchAction == ERQueryService.DOWNSTREAM) {
            searchActionIsRight = body.has("dlt");
        } else if (searchAction == ERQueryService.UPSTREAM) {
            searchActionIsRight = body.has("ult");
        } else if (searchAction == ERQueryService.DOWNANDUPSTREAM) {
            searchActionIsRight = body.has("dlt") && body.has("ult");
        }
        assertEquals(searchActionIsRight, true);
    }

    @Test
    public void queryParamsTest() throws URISyntaxException {
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap(3);
        expectedQueryParams.add("limit", "10");
        expectedQueryParams.add("levels", "5");
        expectedQueryParams.add("tree", "true");
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents result = builder.queryParams(expectedQueryParams).build();
        assertEquals("limit=10&levels=5&tree=true", result.getQuery());
        assertEquals(expectedQueryParams, result.getQueryParams());
    }

    @Test
    public void uriParamAndHeaderTest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Forwarded", "proto=https; host=127.0.0.1");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setRequestURI("/search/6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");

        HttpRequest httpRequest = new ServletServerHttpRequest(request);
        UriComponents result = UriComponentsBuilder.fromHttpRequest(httpRequest).build();

        assertEquals("https", result.getScheme());
        assertEquals("127.0.0.1", result.getHost());
        assertEquals("/search/6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", result.getPath());
        assertEquals("https://127.0.0.1/search/6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", result.toUriString());
    }

}
