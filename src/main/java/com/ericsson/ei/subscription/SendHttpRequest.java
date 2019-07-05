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
package com.ericsson.ei.subscription;

import com.ericsson.ei.exception.AuthorizationException;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

/**
 * This class is responsible to send HTTP requests when handling subscriptions.
 */
@Component
public class SendHttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendHttpRequest.class);

    private RestOperations rest;

    public SendHttpRequest(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    /**
     * This method is responsible to notify the subscriber through REST POST with raw body and form
     * parameters.
     *
     * @param notificationMeta
     * @param mapNotificationMessage
     * @param headers
     * @return integer
     * @throws AuthorizationException
     */
    public boolean postDataMultiValue(String notificationMeta, MultiValueMap<String, String> mapNotificationMessage,
            HttpHeaders headers) throws AuthorizationException {
        ResponseEntity<JsonNode> response;

        try {
            LOGGER.info("Performing HTTP request to url: {}", notificationMeta);
            boolean isApplicationXWwwFormUrlEncoded = headers.getContentType()
                    .equals(MediaType.APPLICATION_FORM_URLENCODED);
            if (isApplicationXWwwFormUrlEncoded) {
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapNotificationMessage, headers);
                response = rest.postForEntity(notificationMeta, request, JsonNode.class);
            } else {
                HttpEntity<String> request = new HttpEntity<>(String.valueOf((mapNotificationMessage.get("")).get(0)),
                        headers);
                response = rest.postForEntity(notificationMeta, request, JsonNode.class);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                String message = String.format("Failed to perform HTTP request due to '%s'."
                        + "\nEnsure that you use correct authenticationType, username and password."
                        + "\nDue to authentication error EI will not perform retries.", e.getMessage());
                LOGGER.error(message, e);
                throw new AuthorizationException(message, e);
            }
            LOGGER.error("HTTP-request failed, bad request! When trying to connect to URL: {}\n{}\n{}",
                    notificationMeta, e.getMessage(), e);
            return false;
        } catch (HttpServerErrorException e) {
            LOGGER.error("HTTP-request failed, internal server error!\n When trying to connect to URL: {}\n{}\n{}",
                    notificationMeta, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        HttpStatus status = response.getStatusCode();
        boolean httpStatusSuccess = status == HttpStatus.OK || status == HttpStatus.ACCEPTED
                || status == HttpStatus.CREATED;

        JsonNode body = response.getBody();
        if (httpStatusSuccess) {
            LOGGER.debug("The response status code [" + status + "] and Body: " + body);
        } else {
            LOGGER.debug("POST call failed with status code [" + status + "] and Body: " + body);
        }
        return httpStatusSuccess;
    }

    /**
     * This method performs a get request to given url and with given headers, then returns the result
     * as a ResponseEntity<JsonNode>.
     *
     * @param url
     * @param headers
     * @return
     */
    public ResponseEntity<JsonNode> makeGetRequest(String url, HttpHeaders headers) {
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, request, JsonNode.class);
        return response;

    }
}