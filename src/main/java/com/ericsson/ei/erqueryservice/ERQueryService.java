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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;
import com.ericsson.eiffelcommons.utils.ResponseEntity;

import lombok.Getter;

/**
 * @author evasiba
 */

@Component
public class ERQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ERQueryService.class);

    private HttpRequest request;

    @Getter
    @Value("${er.url}")
    private String erBaseUrl;

    public ERQueryService() {
        this.request = new HttpRequest();
    }

    public void setHttpRequest(HttpRequest request) {
        this.request = request;
    }

    /**
     * This method is used to fetch only the upstream or downstream or both event information from
     * ER based on the eventID and searchOption conditions.
     *
     * @param eventId      the id of the event.
     * @param searchOption the SearchOption to indicate whether to search up, down or both ways from
     *                     the eventId.
     * @param limit        sets the limit of how many events up and/or down stream from the eventId
     *                     to include in the result.
     * @param levels       sets the limit of how many levels up and/or down stream from the eventId
     *                     to include in the result.
     * @param tree         whether or not to retain the tree structure in the result.
     * @return ResponseEntity
     * @throws PropertyNotFoundException 
     */
    public ResponseEntity getEventStreamDataById(String eventId, SearchOption searchOption,
            int limit,
            int levels, boolean tree) throws PropertyNotFoundException, Exception {

        String uri = null;
        try {
            ResponseEntity requestToER = sendRequestToER(eventId, searchOption, limit, levels, tree,
                    uri);
            return requestToER;
        } catch (IOException | URISyntaxException e) {
            throw new Exception("Error occurred while executing REST POST to " + uri, e);
        }
    }

    private ResponseEntity sendRequestToER(String eventId, SearchOption searchOption, int limit,
            int levels,
            boolean tree, String uri)
            throws IOException, URISyntaxException, ClientProtocolException,
            PropertyNotFoundException {
        if (StringUtils.isBlank(erBaseUrl)) {
            throw new PropertyNotFoundException("The URL to ER is not provided");
        }

        final SearchParameters searchParameters = getSearchParameters(searchOption);
        request
               .setHttpMethod(HttpMethod.POST)
               .setBaseUrl(erBaseUrl)
               .setEndpoint(eventId)
               .addParam("limit", Integer.toString(limit))
               .addParam("levels", Integer.toString(levels))
               .addParam("tree", Boolean.toString(tree))
               .setBody(searchParameters.getAsJsonString(), ContentType.APPLICATION_JSON);

        uri = request.getURI().toString();
        LOGGER.debug("The URL to ER is: {}", uri);

        return request.performRequest();
    }

    /**
     * Build the search parameters to be used to query ER.
     *
     * @param searchOption one of UP_STREAM, DOWN_STREAM or UP_AND_DOWN_STREAM
     * @return the search parameters to be used
     */
    private SearchParameters getSearchParameters(SearchOption searchOption) {
        final SearchParameters searchParameters = new SearchParameters();
        final List<LinkType> allLinkTypes = Collections.singletonList(LinkType.ALL);
        switch (searchOption) {
        case DOWN_STREAM:
            searchParameters.setUpstreamLinkType(new ArrayList<>());
            searchParameters.setDownstreamLinkType(allLinkTypes);
            break;
        case UP_STREAM:
            searchParameters.setUpstreamLinkType(allLinkTypes);
            searchParameters.setDownstreamLinkType(new ArrayList<>());
            break;
        case UP_AND_DOWN_STREAM:
            searchParameters.setUpstreamLinkType(allLinkTypes);
            searchParameters.setDownstreamLinkType(allLinkTypes);
            break;
        }

        return searchParameters;
    }
}
