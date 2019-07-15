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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class UrlParserTest {

    private static final String BASE_URL = "http://www.somehost.com";
    private static final String BASE_URL_HTTPS = "https://www.somehost.com";
    private static final String URL_WITH_CONTEXT_PATH = "http://www.somehost.com/some-endpoint/";
    private static final String URL_WITH_HTTPS = "https://www.somehost.com/some-endpoint/";
    private static final String URL_WITH_PARAMS = "http://www.somehost.com/some-endpoint/?param1='my_token'&param2=something_else";

    @Mock
    JmesPathInterface jmesPathInterface;

    @InjectMocks
    UrlParser urlParser;

    @Before
    public void beforeTests() throws IOException {

    }

    /**
     * Ensure UrlParser can parse different kinds of urls.
     *
     * @throws Exception
     */
    @Test
    public void testExtractBaseUrl() throws Exception {
        assertEquals(BASE_URL, urlParser.extractBaseUrl(BASE_URL));
        assertEquals(BASE_URL, urlParser.extractBaseUrl(URL_WITH_CONTEXT_PATH));
        assertEquals(BASE_URL, urlParser.extractBaseUrl(URL_WITH_PARAMS));

        assertEquals(BASE_URL_HTTPS, urlParser.extractBaseUrl(BASE_URL_HTTPS));
        assertEquals(BASE_URL_HTTPS, urlParser.extractBaseUrl(URL_WITH_HTTPS));
    }

    @Test
    public void runJmesPathOnParametersTest() throws Exception {
        // TODO: ...
    }

}
