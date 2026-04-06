
package com.ericsson.ei.controller.model;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
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
    "queryResponseEntity"
})
public class QueryResponse {

    @JsonProperty("queryResponseEntity")
    @Valid
    private QueryResponseEntity queryResponseEntity;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("queryResponseEntity")
    public QueryResponseEntity getQueryResponseEntity() {
        return queryResponseEntity;
    }

    @JsonProperty("queryResponseEntity")
    public void setQueryResponseEntity(QueryResponseEntity queryResponseEntity) {
        this.queryResponseEntity = queryResponseEntity;
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
    public String toString() {
        return new ToStringBuilder(this).append("queryResponseEntity", queryResponseEntity).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(queryResponseEntity).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof QueryResponse) == false) {
            return false;
        }
        QueryResponse rhs = ((QueryResponse) other);
        return new EqualsBuilder().append(queryResponseEntity, rhs.queryResponseEntity).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
