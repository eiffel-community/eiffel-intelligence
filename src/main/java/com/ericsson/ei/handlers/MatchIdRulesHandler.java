package com.ericsson.ei.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    @Autowired
    private ObjectHandler objHandler;

    public String fetchObjectById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        String fetchQuerry = replaceIdInRules(matchIdString, id);
        return objHandler.findObjectById(fetchQuerry);
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