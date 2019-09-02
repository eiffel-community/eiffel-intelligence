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
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class representation of the POST body to the search API's upstream/downstream method.
 */
public class SearchParameters {

    private List<LinkType> downstreamLinkType;
    private List<LinkType> upstreamLinkType;

    public SearchParameters() {
    }

    public SearchParameters(final List<LinkType> downstreamLinkType, final List<LinkType> upstreamLinkType) {
        this.downstreamLinkType = downstreamLinkType;
        this.upstreamLinkType = upstreamLinkType;
    }

    private List<LinkType> checkForAll(final List<LinkType> list) {
        if (list.size() == 1 && list.get(0).equals(LinkType.ALL)) {
            return Arrays.asList(LinkType.values());
        } else {
            return list;
        }
    }

    public List<LinkType> getDownstreamLinkType() {
        return checkForAll(downstreamLinkType);
    }

    public void setDownstreamLinkType(final List<LinkType> downstreamLinkType) {
        this.downstreamLinkType = downstreamLinkType;
    }

    public List<LinkType> getUpstreamLinkType() {
        return checkForAll(upstreamLinkType);
    }

    public void setUpstreamLinkType(final List<LinkType> upstreamLinkType) {
        this.upstreamLinkType = upstreamLinkType;
    }

    /**
     * Returns the search parameters as a json string
     * @return String
     * @throws IOException
     */
    public String getAsJsonString() throws IOException {
        ArrayList<String> downstreamLinkTypeStringArray = convertSearchParametersToArrayList(downstreamLinkType);
        ArrayList<String> upstreamLinkTypeStringArray = convertSearchParametersToArrayList(upstreamLinkType);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode downloadLinkTypeJson = mapper.readTree(downstreamLinkTypeStringArray.toString());
        JsonNode upstreamLinkTypeJson = mapper.readTree(upstreamLinkTypeStringArray.toString());

        return "{\"dlt\":" + downloadLinkTypeJson.toString() + ",\"ult\":" + upstreamLinkTypeJson.toString() + "}";
    }

    @Override
    public String toString() {
        return "SearchParameters{" + "dlt=" + downstreamLinkType + ", ult=" + upstreamLinkType + '}';
    }

    /**
     * Converts the searchParameters to a ArrayList with json
     * @param searchParameters
     * @return
     */
    private ArrayList<String> convertSearchParametersToArrayList(List<LinkType> searchParameters) {
        Object[] searchParametersArray = searchParameters.toArray();
        ArrayList<String> searchParametersJsonStringArray = new ArrayList<String>();

        for(int i = 0; i < searchParametersArray.length; i++) {
            String linkTypeValue = "\"" + searchParametersArray[i].toString() + "\"";
            searchParametersJsonStringArray.add(linkTypeValue);
        }

        return searchParametersJsonStringArray;
    }
}
