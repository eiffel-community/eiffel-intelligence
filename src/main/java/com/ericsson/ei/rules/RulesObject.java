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

    public String getTemplateName() {
        JsonNode jsonNode = rulesObject.get("TemplateName");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getMatchIdRules() {
        return getString("MatchIdRules");
    }

    public String getIdRule() {
        return getTextValue("IdRule");
    }

    public String getIdentifyRules() {
        return getTextValue("IdentifyRules");
    }

    public String getExtractionRules() {
        return getTextValue("ExtractionRules");
    }

    public String getMergeRules() {
        return getTextValue("MergeResolverRules");
    }

    public String getDownstreamIdentifyRules() {
        return getTextValue("DownstreamIdentifyRules");
    }

    public String getDownstreamExtractionRules() {
        return getTextValue("DownstreamExtractionRules");
    }

    public String getDownstreamMergeRules() {
        return getTextValue("DownstreamMergeRules");
    }

    public String fetchProcessRules() {
        return getTextValue("ProcessRules");
    }

    public String getTextValue(String fieldName) {
         JsonNode jsonNode = rulesObject.get(fieldName);
         if (jsonNode != null)
             return jsonNode.textValue();
         return null;
    }

    public String getString(String fieldName) {
        JsonNode jsonNode = rulesObject.get(fieldName);
        if (jsonNode != null)
            return jsonNode.toString();
        return null;
   }

    public boolean equals(Object other) {
        if (other instanceof RulesObject) {
            return rulesObject.equals(((RulesObject) other).getJsonRulesObject());
        }

        return (this == other);
    }

    public boolean isStartEventRules() {
        String value = rulesObject.get("StartEvent").textValue().toLowerCase();
        return value.equals("yes");
    }
}
