
package com.ericsson.ei.controller.model;

import java.util.HashMap;
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
    "criteria",
    "options",
    "filterKey"
})
public class QueryBody {

    @JsonProperty("criteria")
    private Criteria criteria;
    @JsonProperty("options")
    private Options options;
    @JsonProperty("filterKey")
    private FilterKey filterKey;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("criteria")
    public Criteria getCriteria() {
        return criteria;
    }

    @JsonProperty("criteria")
    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    @JsonProperty("options")
    public Options getOptions() {
        return options;
    }

    @JsonProperty("options")
    public void setOptions(Options options) {
        this.options = options;
    }

    @JsonProperty("filterKey")
    public FilterKey getFilterKey() {
        return filterKey;
    }

    @JsonProperty("filterKey")
    public void setFilterKey(FilterKey filterKey) {
        this.filterKey = filterKey;
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
        return new HashCodeBuilder().append(criteria).append(options).append(filterKey).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof QueryBody) == false) {
            return false;
        }
        QueryBody rhs = ((QueryBody) other);
        return new EqualsBuilder().append(criteria, rhs.criteria).append(options, rhs.options).append(filterKey, rhs.filterKey).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
