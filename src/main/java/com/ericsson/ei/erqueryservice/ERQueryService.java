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
package com.ericsson.ei.erqueryservice;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ERQueryService {

    static Logger log = (Logger) LoggerFactory.getLogger(ERQueryService.class);

    @Value("${er.url}")
    private String url;

    private RestOperations rest;

    public ERQueryService(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    /**
     * This method only extracts the event information from ER2.0 based on the
     * eventID.
     * 
     * @param eventId
     * @return ResponseEntity
     */

    public ResponseEntity getEventDataById(String eventId) {
        String erUrl = url + "{id}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", eventId);
        ResponseEntity<String> response = null;
        log.info("The ID parameter is set");
        try {
            response = rest.getForEntity(erUrl, String.class, params);
            log.debug("The response is : " + response.toString());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Got RESOURCE NOT FOUND error");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    /**
     * This method fetches the upstream and downstream event information from
     * ER2.0 based on the eventID.
     * 
     * @param eventId
     * @param searchParameters
     * @param limitParam
     * @param levelsParam
     * @param tree
     * @return ResponseEntity
     */

    public ResponseEntity getEventSteamDataById(String eventId, JsonNode searchParameters, int limitParam,
            int levelsParam, boolean tree) {
        String erUrl = url + eventId;

        // URI (URL) parameters
        Map<String, Object> uriParams = new HashMap<String, Object>();
        uriParams.put("searchParameters", searchParameters);

        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(erUrl).queryParam("limit", limitParam)
                .queryParam("levels", levelsParam).queryParam("tree", tree);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(uriParams, headers);

        ResponseEntity response = rest.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.POST,
                requestEntity, JsonNode.class);
        return response;
    }

}
