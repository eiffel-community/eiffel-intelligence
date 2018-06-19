
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
    "foundSubscriptions",
    "notFoundSubscriptions"
})
public class GetSubscriptionResponse {

    @JsonProperty("foundSubscriptions")
    private List<Subscription> foundSubscriptions = new ArrayList<Subscription>();
    @JsonProperty("notFoundSubscriptions")
    private List<String> notFoundSubscriptions = new ArrayList<String>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("foundSubscriptions")
    public List<Subscription> getFoundSubscriptions() {
        return foundSubscriptions;
    }

    @JsonProperty("foundSubscriptions")
    public void setFoundSubscriptions(List<Subscription> foundSubscriptions) {
        this.foundSubscriptions = foundSubscriptions;
    }

    @JsonProperty("notFoundSubscriptions")
    public List<String> getNotFoundSubscriptions() {
        return notFoundSubscriptions;
    }

    @JsonProperty("notFoundSubscriptions")
    public void setNotFoundSubscriptions(List<String> notFoundSubscriptions) {
        this.notFoundSubscriptions = notFoundSubscriptions;
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
        return new HashCodeBuilder().append(foundSubscriptions).append(notFoundSubscriptions).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetSubscriptionResponse) == false) {
            return false;
        }
        GetSubscriptionResponse rhs = ((GetSubscriptionResponse) other);
        return new EqualsBuilder().append(foundSubscriptions, rhs.foundSubscriptions).append(notFoundSubscriptions, rhs.notFoundSubscriptions).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
