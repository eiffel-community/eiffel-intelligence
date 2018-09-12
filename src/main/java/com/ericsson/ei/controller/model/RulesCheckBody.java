
package com.ericsson.ei.controller.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "listRulesJson",
    "listEventsJson"
})
public class RulesCheckBody {

    @JsonProperty("listRulesJson")
    private List<Object> listRulesJson = new ArrayList<Object>();
    @JsonProperty("listEventsJson")
    private List<Object> listEventsJson = new ArrayList<Object>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("listRulesJson")
    public List<Object> getListRulesJson() {
        return listRulesJson;
    }

    @JsonProperty("listRulesJson")
    public void setListRulesJson(List<Object> listRulesJson) {
        this.listRulesJson = listRulesJson;
    }

    @JsonProperty("listEventsJson")
    public List<Object> getListEventsJson() {
        return listEventsJson;
    }

    @JsonProperty("listEventsJson")
    public void setListEventsJson(List<Object> listEventsJson) {
        this.listEventsJson = listEventsJson;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(listRulesJson).append(listEventsJson).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RulesCheckBody) == false) {
            return false;
        }
        RulesCheckBody rhs = ((RulesCheckBody) other);
        return new EqualsBuilder().append(listRulesJson, rhs.listRulesJson).append(listEventsJson, rhs.listEventsJson).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
