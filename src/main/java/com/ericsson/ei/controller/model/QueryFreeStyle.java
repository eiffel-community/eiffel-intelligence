package com.ericsson.ei.controller.model;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "freestylequery"
})
@EqualsAndHashCode
@ToString
public class QueryFreeStyle {

    @JsonProperty("freestylequery")
    private String freeStyleQuery;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("freestylequery")
    public String getFreeStyleQuery() {
        return freeStyleQuery;
    }

    @JsonProperty("freestylequery")
    public void setFreeStyleQuery(String freeStyleQuery) {
        this.freeStyleQuery = freeStyleQuery;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
