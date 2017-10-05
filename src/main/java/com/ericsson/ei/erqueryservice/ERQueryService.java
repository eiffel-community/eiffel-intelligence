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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author evasiba
 *
 */
@Component
public class ERQueryService {

    static Logger log = (Logger) LoggerFactory.getLogger(ERQueryService.class);

    private RestOperations rest;

    public final static int DOWNSTREAM = 0;
    public final static int UPSTREAM = 1;
    public final static int DOWNANDUPSTREAM = 2;

    @Value("${er.url}")
    private String url;

    public String getUrl() {
        return url;
    }

    public ERQueryService(RestTemplateBuilder builder) {
        rest = builder.build();
    }

    public void setRest(RestOperations rest) {
        this.rest = rest;
    }

    /**
     * This method only extracts the event information from ER2.0 based on the
     * eventID.
     *
     * @param eventId
     * @return ResponseEntity
     */
    public ResponseEntity getEventDataById(String eventId) {
        String erUrl = url.trim() + "{id}";
        log.info("The url is : " + erUrl);
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", eventId);
        ResponseEntity<String> response = null;
        log.info("The ID parameter is set");
        try {
            response = rest.getForEntity(erUrl, String.class, params);
            log.info("The response is : " + response.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    /**
     * This method is used to fetch only the upstream or downstream or both
     * event information from ER2.0 based on the eventID and searchAction
     * conditions.
     *
     * @param eventId
     * @param searchAction
     * @param limitParam
     * @param levelsParam
     * @param tree
     * @return ResponseEntity
     */

    public ResponseEntity getEventStreamDataById(String eventId, int searchAction, int limitParam,
            int levelsParam, boolean tree) {

        String erUrl = url.trim() + eventId;
        log.info("The url is : " + erUrl);

        // Request Body parameters
        JsonNode uriParams = getSearchParameters(searchAction);

        // Add query parameter
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(erUrl).queryParam("limit", limitParam)
                .queryParam("levels", levelsParam).queryParam("tree", tree);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(uriParams, headers);
        log.info("The request is : " + builder.buildAndExpand(uriParams).toUri().toString());

        ResponseEntity response = rest.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.POST,
                requestEntity, JsonNode.class);
        return response;
    }

    /** Generates the json object used as body for downstream/upstream
     * query requests
     * @param searchAction - one of DOWNSTREAM, UPSTREAM or DOWNANDUPSTREAM
     * @return
     */
    public JsonNode getSearchParameters(int searchAction) {
        JsonNode uriParams = null;
        ObjectMapper objectmapper = new ObjectMapper();

        String[] linkTypes = {"ALL"};

        try {
            uriParams = objectmapper.readTree("{}");
            if (searchAction == DOWNSTREAM) {
                putSearchParameter(uriParams, "dlt", linkTypes);
            } else if (searchAction == UPSTREAM) {
                putSearchParameter(uriParams, "ult", linkTypes);
            } else if (searchAction == DOWNANDUPSTREAM) {
                putSearchParameter(uriParams, "dlt", linkTypes);
                putSearchParameter(uriParams, "ult", linkTypes);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return uriParams;
    }

    /** Create an array node with link types for upstream or downstream query
     * @param params
     * @param actionString
     * @param linkTypes
     */
    public void putSearchParameter(JsonNode params, String actionString, String[] linkTypes) {
        ArrayNode node =((ObjectNode) params).putArray(actionString);
        for (String string : linkTypes) {
            node.add(string);
        }
    }

    @PostConstruct
    public void init() {
        log.debug("The url parameter is : " + url);
    }
}
