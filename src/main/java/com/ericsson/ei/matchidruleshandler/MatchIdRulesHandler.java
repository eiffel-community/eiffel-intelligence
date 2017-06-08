package com.ericsson.ei.matchidruleshandler;

import org.springframework.stereotype.Component;
import com.ericsson.ei.rules.RulesObject;

@Component
public class MatchIdRulesHandler {

    public void fetchObjectById(RulesObject ruleObject, String id) {
        String matchIdString = ruleObject.getMatchIdRules();
        String replacedMatchId = MatchIdRulesHandler.replaceIdInRules(matchIdString, id);
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