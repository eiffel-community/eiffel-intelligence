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
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    @Autowired
    private ObjectHandler objHandler;

    @Autowired
    private EventToObjectMapHandler eventToObjectMapHandler;

    public List<String> fetchObjectsById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        String fetchQuery = replaceIdInRules(matchIdString, id);
        List<String> objects = objHandler.findObjectsByCondition(fetchQuery);
        if (objects.isEmpty()) {
            List<String> objectIds = eventToObjectMapHandler.getObjectsForEventId(id);
            objects = objHandler.findObjectsByIds(objectIds);
        }
        return objects;
    }

    public static String replaceIdInRules(String matchIdString, String id) {
        if (matchIdString.contains("%IdentifyRules%")) {
            return matchIdString.replace("%IdentifyRules%", id);
        } else if (matchIdString.contains("%IdentifyRules_objid%")) {
            return matchIdString.replace("%IdentifyRules_objid%", id);
        } else
            return null;
    }

}