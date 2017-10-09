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
        JsonNode jsonNode = rulesObject.get("MatchIdRules");
        if (jsonNode != null)
            return jsonNode.toString();
        return "";
    }

    public String getIdRule() {
        JsonNode jsonNode = rulesObject.get("IdRule");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getIdentifyRules() {
        JsonNode jsonNode = rulesObject.get("IdentifyRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getExtractionRules() {
        JsonNode jsonNode = rulesObject.get("ExtractionRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getMergeRules() {
        JsonNode jsonNode = rulesObject.get("MergeResolverRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getDownstreamIdentifyRules() {
        JsonNode jsonNode = rulesObject.get("DownstreamIdentifyRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getDownstreamExtractionRules() {
        JsonNode jsonNode = rulesObject.get("DownstreamExtractionRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String getDownstreamMergeRules() {
        JsonNode jsonNode = rulesObject.get("DownstreamMergeRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
    }

    public String fetchProcessRules() {
        JsonNode jsonNode = rulesObject.get("ProcessRules");
        if (jsonNode != null)
            return jsonNode.textValue();
        return "";
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
