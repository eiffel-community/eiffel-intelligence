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
package com.ericsson.ei.handlers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.mongo.MongoStringQuery;
import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    @Value("${mergeidmarker:%IdentifyRules%}")
    private String mergeIdMarker;

    @Autowired
    private ObjectHandler objHandler;

    /**
     * This method searches the database for any aggregated objects matching the search condition
     * written in the rules MatchIdRules.
     * */
    public List<String> fetchObjectsById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        String fetchQueryString = replaceIdInRules(matchIdString, id);
        MongoQuery fetchQuery = new MongoStringQuery(fetchQueryString);
        List<String> objects = objHandler.findObjectsByCondition(fetchQuery);
        return objects;
    }

    /**
     * This method replaces the 'mergeidmarker' placeholder in MatchIdRules with a given
     * Eiffel event id. The 'mergeidmarker' property is defined in application.properties.
     *
     * If 'mergeidmarker' is defined as:'%myPlaceHolderId%', and this method is called with the
     * matchIdString: {"_id": "%myPlaceHolderId%"} and the id: aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee0
     * the updated string will look like: {"_id": "aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee0"}
     *
     * @param matchIdString  the string containing a placeholder key to be replaced
     * @param id             the Eiffel event id to replace placeholder with
     * @return
     * */
    public String replaceIdInRules(String matchIdString, String id) {
        if (matchIdString.contains(mergeIdMarker)) {
            return matchIdString.replace(mergeIdMarker, id);
        } else {
            return null;
        }
    }

}