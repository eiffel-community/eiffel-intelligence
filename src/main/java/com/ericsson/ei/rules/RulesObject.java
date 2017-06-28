package com.ericsson.ei.rules;

import com.fasterxml.jackson.databind.JsonNode;

public class RulesObject {
    private JsonNode rulesObject;

    public RulesObject(JsonNode rulesObject) {
        super();
        this.rulesObject = rulesObject;
    }

    public JsonNode getJsonRulesObject() {
        return rulesObject;
    }

    public String getMatchIdRules() {
        return rulesObject.get("MatchIdRules").toString();
    }

    public String getExtractionRules() {
        return rulesObject.get("ExtractionRules").textValue();
    }

    public String getMergeRules() {
        return rulesObject.get("MergeResolverRules").textValue();
    }

    public boolean equals(Object other){
        if (other instanceof RulesObject) {
            return rulesObject.equals(((RulesObject)other).getJsonRulesObject());
        }

        return (this == other);
    }
}
