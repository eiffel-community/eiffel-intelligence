package com.ericsson.ei.handlers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    @Autowired
    private ObjectHandler objHandler;

    @Autowired
    private EventToObjectMapHandler eventToObjectMapHandler;

    public ArrayList<String> fetchObjectsById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        String fetchQuerry = replaceIdInRules(matchIdString, id);
        ArrayList<String> objects = objHandler.findObjectsByCondition(fetchQuerry);
        if (objects.isEmpty()) {
            ArrayList<String> objectIds = eventToObjectMapHandler.getObjectsForEventId(id);
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