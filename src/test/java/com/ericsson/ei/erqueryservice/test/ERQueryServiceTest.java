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
import java.net.URISyntaxException;
import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ERQueryServiceTest {

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
