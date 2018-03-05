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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

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

    public String getHistoryExtractionRules() {
        return rulesObject.get("HistoryExtractionRules").textValue();
    }

    public String getHistoryPathRules() {
        return getTextValue("HistoryPathRules");
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
        return "";
   }

    public boolean equals(Object other) {
        if (other instanceof RulesObject) {
            return rulesObject.equals(((RulesObject) other).getJsonRulesObject());
        }

        return (this == other);
    }

    public boolean isStartEventRules() {
        return hasStringProperty("StartEvent", "yes");
    }

    public boolean isNeedHistoryRule() {
        return hasStringProperty("NeedHistoryRule", "yes");
    }

    private boolean hasStringProperty(final String key, final String value) {
        final JsonNode node = rulesObject.get(key);
        return node != null && !(node instanceof NullNode) && node.asText().equalsIgnoreCase(value);
    }
}
