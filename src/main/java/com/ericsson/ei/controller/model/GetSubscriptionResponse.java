
package com.ericsson.ei.controller.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
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
    @Valid
    private List<Subscription> foundSubscriptions = new ArrayList<Subscription>();
    @JsonProperty("notFoundSubscriptions")
    @Valid
    private List<String> notFoundSubscriptions = new ArrayList<String>();
    @JsonIgnore
    @Valid
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
        return new ToStringBuilder(this).append("foundSubscriptions", foundSubscriptions).append("notFoundSubscriptions", notFoundSubscriptions).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notFoundSubscriptions).append(additionalProperties).append(foundSubscriptions).toHashCode();
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
        return new EqualsBuilder().append(notFoundSubscriptions, rhs.notFoundSubscriptions).append(additionalProperties, rhs.additionalProperties).append(foundSubscriptions, rhs.foundSubscriptions).isEquals();
    }

}
