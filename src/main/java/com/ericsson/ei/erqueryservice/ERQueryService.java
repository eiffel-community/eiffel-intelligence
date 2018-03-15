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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author evasiba
 */
@Component
public class ERQueryService {

    static Logger log = (Logger) LoggerFactory.getLogger(ERQueryService.class);
    private RestOperations rest;
    @Getter
    @Value("${er.url}")
    private String url;

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
     *            the id of the event.
     * @return ResponseEntity
     */
    public ResponseEntity<String> getEventDataById(String eventId) {
        final String erUrl = URI.create(url.trim() + "/" + "{id}").normalize().toString();
        log.debug("The URL to ER is: " + erUrl);

        final Map<String, String> params = Collections.singletonMap("id", eventId);
        log.trace("The ID parameter is set");
        try {
            final ResponseEntity<String> response = rest.getForEntity(erUrl, String.class, params);
            log.trace("The response is : " + response.toString());
        } catch (RestClientException e) {
            log.error("Error occurred while executing REST GET to: " + erUrl + " for " + eventId, e);
        }

        return null;
    }

    /**
     * This method is used to fetch only the upstream or downstream or both
     * event information from ER2.0 based on the eventID and searchOption
     * conditions.
     *
     * @param eventId
     *            the id of the event.
     * @param searchOption
     *            the SearchOption to indicate whether to search up, down or
     *            both ways from the eventId.
     * @param limit
     *            sets the limit of how many events up and/or down stream from
     *            the eventId to include in the result.
     * @param levels
     *            sets the limit of how many levels up and/or down stream from
     *            the eventId to include in the result.
     * @param tree
     *            whether or not to retain the tree structure in the result.
     * @return ResponseEntity
     */
    public ResponseEntity<JsonNode> getEventStreamDataById(String eventId, SearchOption searchOption, int limit,
            int levels, boolean tree) {

        final String erUrl = URI.create(url.trim() + "/" + eventId).normalize().toString();
        log.debug("The URL to ER is: " + erUrl);

        // Request Body parameters
        final SearchParameters searchParameters = getSearchParameters(searchOption);

        // Build query parameters
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(erUrl).queryParam("limit", limit)
                .queryParam("levels", levels).queryParam("tree", tree);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<SearchParameters> requestEntity = new HttpEntity<>(searchParameters, headers);
        final UriComponents uriComponents = builder.buildAndExpand(searchParameters);
        log.debug("The request is : " + uriComponents.toUri().toString());

        try {
            return rest.exchange(uriComponents.toUri(), HttpMethod.POST, requestEntity, JsonNode.class);
        } catch (RestClientException e) {
            log.error("Error occurred while executing REST POST to: " + erUrl + " for\n" + requestEntity, e);
        }

        return null;
    }

    /**
     * Build the search parameters to be used to query ER.
     *
     * @param searchOption
     *            one of UP_STREAM, DOWN_STREAM or UP_AND_DOWN_STREAM
     * @return the search parameters to be used
     */
    private SearchParameters getSearchParameters(SearchOption searchOption) {
        final SearchParameters searchParameters = new SearchParameters();
        final List<LinkType> allLinkTypes = Collections.singletonList(LinkType.ALL);
        switch (searchOption) {
        case DOWN_STREAM:
            searchParameters.setUlt(new ArrayList<>());
            searchParameters.setDlt(allLinkTypes);
            break;
        case UP_STREAM:
            searchParameters.setUlt(allLinkTypes);
            searchParameters.setDlt(new ArrayList<>());
            break;
        case UP_AND_DOWN_STREAM:
            searchParameters.setUlt(allLinkTypes);
            searchParameters.setDlt(allLinkTypes);
            break;
        }

        return searchParameters;
    }

    @PostConstruct
    public void init() {
        // TODO: is this needed?
        log.debug("The url parameter is : " + url);
    }
}
