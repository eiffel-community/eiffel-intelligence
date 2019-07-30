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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsCrumbTest {

    private static final String URL = "http://www.somehot.com/some-endpoint/";
    private static final String BASE_URL = "http://www.somehot.com";
    private static final String JENKINS_CRUMB_URL = "http://www.somehot.com/crumbIssuer/api/json";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private final ObjectMapper mapper = new ObjectMapper();

    private String encoding;

    private HttpHeaders headers = new HttpHeaders();

    @Mock
    HttpRequestSender httpRequestSender;

    @Mock
    UrlParser urlParser;

    @InjectMocks
    private JenkinsCrumb jenkinsCrumb;

    @Before
    public void beforeTests() throws IOException {
        encoding = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
        when(urlParser.extractBaseUrl(URL)).thenReturn(BASE_URL);

        headers.add("Authorization", "Basic " + encoding);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Ensures that jenkins crumb url is correctly built and request is ok, then the return value
     * should contain the raw JsonNode data.
     *
     * @throws Exception
     */
    @Test
    public void correctJenkinsCrumbIsReturned() throws Exception {
        String key = "test_key";
        String value = "test_value";
        String responseData = "{\"" + key + "\":\"" + value + "\"}";
        final JsonNode responseDataNode = mapper.readValue(responseData, JsonNode.class);
        ResponseEntity<JsonNode> response = new ResponseEntity<JsonNode>(responseDataNode,
                HttpStatus.OK);

        when(httpRequestSender.makeGetRequest(JENKINS_CRUMB_URL, headers)).thenReturn(response);

        JsonNode responseNode = jenkinsCrumb.fetchJenkinsCrumb(encoding, URL);

        assertEquals("Crumb value is equal to fetched value", value,
                responseNode.get(key).asText());
    }

    /**
     * Jenkins returns exception with status unauthorized, thus we throw AuthenticationException.
     *
     * @throws Exception
     */
    @Test(expected = AuthenticationException.class)
    public void fetchJenkinsCrumbThrowsAuthException() throws Exception {
        when(httpRequestSender.makeGetRequest(any(), any())).thenThrow(
                new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        jenkinsCrumb.fetchJenkinsCrumb(encoding, URL);
    }

    /**
     * Jenkins returns exception with status not found, thus we return null.
     *
     * @throws Exception
     */
    @Test
    public void fetchJenkinsCrumbReturnsNull() throws Exception {
        when(httpRequestSender.makeGetRequest(any(), any())).thenThrow(
                new HttpClientErrorException(HttpStatus.NOT_FOUND));
        JsonNode responseNode = jenkinsCrumb.fetchJenkinsCrumb(encoding, URL);

        assertNull(responseNode);
    }

}
