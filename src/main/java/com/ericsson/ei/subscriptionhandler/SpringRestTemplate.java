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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Base64;

import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible to send notification through REST POST to the
 * recipient of the Subscription Object.
 */

@Component
public class SpringRestTemplate {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SpringRestTemplate.class);

    private RestOperations rest;

    public SpringRestTemplate(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    /**
     * This method is responsible to notify the subscriber through REST POST With raw body and form parameters.
     *
     * @param notificationMeta
     * @param mapNotificationMessage
     * @param headerContentMediaType
     * @return integer
     */
    public int postDataMultiValue(String notificationMeta, MultiValueMap<String, String> mapNotificationMessage, String headerContentMediaType, String ...args) {
        ResponseEntity<JsonNode> response;
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(headerContentMediaType));
            if (headerContentMediaType.equals(MediaType.APPLICATION_FORM_URLENCODED.toString())) { //"application/x-www-form-urlencoded"
                
                if(notificationMeta.contains("jenkins") && args.length != 0) { 
                    String user = args[0];   
                    String password = args[1];
                    String notEncoded = user + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
                    headers.add("Authorization", "Basic " + encodedAuth);                    
                }
                
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapNotificationMessage, headers);
                response = rest.postForEntity(notificationMeta, request, JsonNode.class);
            } else {
                HttpEntity<String> request = new HttpEntity<>(String.valueOf((mapNotificationMessage.get("")).get(0)), headers);
                response = rest.postForEntity(notificationMeta, request, JsonNode.class);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            try {
                return Integer.parseInt(e.getMessage());
            } catch (NumberFormatException error) {
                return HttpStatus.NOT_FOUND.value();
            }
        }
        HttpStatus status = response.getStatusCode();
        LOGGER.debug("The response code after POST is : " + status);
        if (status == HttpStatus.OK) {
            JsonNode restCall = response.getBody();
            LOGGER.debug("The response Body is : " + restCall);
        }
        return response.getStatusCode().value();
    }
    
    public void postDataMultiValue(BasicNameValuePair vp, String user, String password) {
        
        String notificationMeta = "https://fem101-eiffel039.lmera.ericsson.se:8443/jenkins/job/test_params/buildWithParameters";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
//        String notEncoded = user + ":" + password;
//        String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.add("Authorization", "Basic " + encodedAuth);

//        BasicNameValuePair vp = new BasicNameValuePair("json","{\"parameter\": [{\"name\":\"parameter\", \"value\":\"taskfile\"},{\"name\":\"parameter2\",\"value\": \"kfile\"}]}");
        HttpEntity<String> request = new HttpEntity<String>(vp.toString(), httpHeaders);
        ResponseEntity<String> personEntity = restTemplate.postForEntity(notificationMeta, request,String.class);
    }
}