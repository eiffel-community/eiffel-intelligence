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

import com.ericsson.ei.exception.AuthenticationException;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

/**
 * This class is responsible to send HTTP requests when handling subscriptions.
 */
@Component
public class HttpRequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestSender.class);

    private RestOperations rest;
    
    public HttpRequestSender(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    /**
     * Sends a REST POST request to the given url.
     *
     * @param url     A String containing the URL to send request to
     * @param request A HTTP POST request
     * @throws AuthenticationException, HttpClientErrorException, HttpServerErrorException,
     *                                  Exception
     */
    public void postDataMultiValue(String url, HttpEntity<?> request)
            throws AuthenticationException, HttpClientErrorException, HttpServerErrorException,
            Exception {
        ResponseEntity<JsonNode> response;

        try {
            LOGGER.info("Performing HTTP request to url: {}", url);
            response = rest.postForEntity(url, request, JsonNode.class);
        } catch (HttpClientErrorException e) {
            checkIfAuthenticationException(e);
            LOGGER.error("HTTP request failed, bad request! When trying to connect to URL: {}\n{}",
                    url, e);
            throw e;
        } catch (HttpServerErrorException e) {
            LOGGER.error("HttpServerErrorException, HTTP request to url {} failed\n", url, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("HTTP request to url {} failed\n", url, e);
            throw e;
        }

        HttpStatus status = response.getStatusCode();
        JsonNode body = response.getBody();
        boolean httpStatusSuccess = status == HttpStatus.OK || status == HttpStatus.ACCEPTED
                || status == HttpStatus.CREATED;
        if (!httpStatusSuccess) {
            Exception e = new Exception("Status: " + status + ", Body: " + body.toString());
            LOGGER.error("HTTP request failed, response status code is [{}] and body: {}", status,
                    body, e);
            throw e;
        }
    }

    /**
     * Validates a given HttpClientErrorException and throws a new AuthenticationException if the
     * exception is caused by and authentication failure.
     *
     * @param e
     * @throws AuthenticationException
     */
    private void checkIfAuthenticationException(HttpClientErrorException e)
            throws AuthenticationException {
        boolean unauthorizedRequest = e.getStatusCode() == HttpStatus.UNAUTHORIZED;
        boolean badRequestForbidden = e.getStatusCode() == HttpStatus.FORBIDDEN;
        if (unauthorizedRequest || badRequestForbidden) {
            String message = String.format("Failed to perform HTTP request due to '%s'."
                    + "\nEnsure that you use correct authenticationType, username and password."
                    + "\nDue to authentication error EI will not perform retries.",
                    e.getMessage());
            LOGGER.error(message, e);
            throw new AuthenticationException(message, e);
        }
    }

    /**
     * This method performs a get request to given url and with given headers, then returns the
     * result as a ResponseEntity<JsonNode>.
     *
     * @param url
     * @param headers
     * @return ResponseEntity
     */
    public ResponseEntity<JsonNode> makeGetRequest(String url, HttpHeaders headers) {
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, request,
                JsonNode.class);
        return response;
    }
}