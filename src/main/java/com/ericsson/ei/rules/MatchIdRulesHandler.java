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
package com.ericsson.ei.rules;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.ei.exception.ReplacementMarkerException;
import com.ericsson.ei.handlers.ObjectHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.mongo.MongoStringQuery;
import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchIdRulesHandler.class);

    @Value("${rules.replacement.marker:%IdentifyRulesEventId%}")
    private String replacementMarker;

    @Autowired
    private ObjectHandler objHandler;

    /**
     * This method searches the database for any aggregated objects matching the search condition
     * written in the rules MatchIdRules.
     * */
    public List<String> fetchObjectsById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        List<String> objects = new ArrayList<>();
        try {
            objects = doFetchObjectsById(matchIdString, id);
        } catch (ReplacementMarkerException e) {
            LOGGER.error("Replacement marker mismatch.", e);
        }
        return  objects;
    }

    /**
     * This method replaces the 'replacementMarker' placeholder in MatchIdRules with a given Eiffel
     * event id. The 'replacementMarker' property is defined in application.properties.
     *
     * <p>
     * If 'replacementMarker' is defined as:'%myPlaceHolderId%', <br />
     * and this method is called with the matchIdString: {"_id": "%myPlaceHolderId%"} <br />
     * and the id: aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee0 <br />
     * the updated string will look like: {"_id":"aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee0"}
     * </p>
     *
     * @param matchIdString the string containing a placeholder key to be replaced
     * @param id            the Eiffel event id to replace placeholder with
     * @return an updated matchIdString
     * @throws ReplacementMarkerException
     */
    protected String replaceIdInRules(String matchIdString, String id) throws ReplacementMarkerException {
        if (matchIdString.contains(replacementMarker)) {
            return matchIdString.replace(replacementMarker, id);
        } else {
            String errorMessage = String.format("MatchIdRules: %s does not contain the rules.replacement.marker: %s", matchIdString, replacementMarker);
            throw new ReplacementMarkerException(errorMessage);
        }
    }

    private List<String> doFetchObjectsById(String matchIdString, String id) throws ReplacementMarkerException {
        String fetchQueryString = replaceIdInRules(matchIdString, id);
        MongoQuery fetchQuery = new MongoStringQuery(fetchQueryString);
        List<String> objects = objHandler.findObjectsByCondition(fetchQuery);
        return objects;
    }
}