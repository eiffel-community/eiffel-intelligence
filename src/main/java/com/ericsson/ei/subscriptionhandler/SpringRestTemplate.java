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
package com.ericsson.ei.subscriptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is responsible to send notification through REST POST to the
 * recipient of the Subscription Object.
 * 
 * @author xjibbal
 *
 */

@Component
public class SpringRestTemplate {

    static Logger log = (Logger) LoggerFactory.getLogger(SpringRestTemplate.class);

    private RestOperations rest;

    public SpringRestTemplate(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    /**
     * This method is responsible to notify the subscriber through REST POST.
     * 
     * @param aggregatedObject
     * @param notificationMeta
     * @return integer
     */
    public int postData(String aggregatedObject, String notificationMeta) {
        JsonNode aggregatedJson = null;
        ResponseEntity<JsonNode> response = null;
        try {
            aggregatedJson = new ObjectMapper().readTree(aggregatedObject);
            response = rest.postForEntity(notificationMeta, aggregatedJson, JsonNode.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return HttpStatus.NOT_FOUND.value();
        }
        HttpStatus status = response.getStatusCode();
        log.info("The response code after POST is : " + status);
        if (status == HttpStatus.OK) {
            JsonNode restCall = response.getBody();
            log.info("The response Body is : " + restCall);
        }
        return response.getStatusCode().value();

    }

}
