
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
    "aggregationtype",
    "created",
    "notificationMessage",
    "notificationMeta",
    "notificationType",
    "repeat",
    "requirements",
    "subscriptionName"
})
public class Subscription {

    @JsonProperty("aggregationtype")
    private String aggregationtype;
    @JsonProperty("created")
    private String created;
    @JsonProperty("notificationMessage")
    private String notificationMessage;
    @JsonProperty("notificationMeta")
    private String notificationMeta;
    @JsonProperty("notificationType")
    private String notificationType;
    @JsonProperty("repeat")
    private Boolean repeat;
    @JsonProperty("requirements")
    private List<Requirement> requirements = new ArrayList<Requirement>();
    @JsonProperty("subscriptionName")
    private String subscriptionName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("aggregationtype")
    public String getAggregationtype() {
        return aggregationtype;
    }

    @JsonProperty("aggregationtype")
    public void setAggregationtype(String aggregationtype) {
        this.aggregationtype = aggregationtype;
    }

    @JsonProperty("created")
    public String getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(String created) {
        this.created = created;
    }

    @JsonProperty("notificationMessage")
    public String getNotificationMessage() {
        return notificationMessage;
    }

    @JsonProperty("notificationMessage")
    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    @JsonProperty("notificationMeta")
    public String getNotificationMeta() {
        return notificationMeta;
    }

    @JsonProperty("notificationMeta")
    public void setNotificationMeta(String notificationMeta) {
        this.notificationMeta = notificationMeta;
    }

    @JsonProperty("notificationType")
    public String getNotificationType() {
        return notificationType;
    }

    @JsonProperty("notificationType")
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    @JsonProperty("repeat")
    public Boolean getRepeat() {
        return repeat;
    }

    @JsonProperty("repeat")
    public void setRepeat(Boolean repeat) {
        this.repeat = repeat;
    }

    @JsonProperty("requirements")
    public List<Requirement> getRequirements() {
        return requirements;
    }

    @JsonProperty("requirements")
    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    @JsonProperty("subscriptionName")
    public String getSubscriptionName() {
        return subscriptionName;
    }

    @JsonProperty("subscriptionName")
    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
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
        return new HashCodeBuilder().append(aggregationtype).append(created).append(notificationMessage).append(notificationMeta).append(notificationType).append(repeat).append(requirements).append(subscriptionName).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Subscription) == false) {
            return false;
        }
        Subscription rhs = ((Subscription) other);
        return new EqualsBuilder().append(aggregationtype, rhs.aggregationtype).append(created, rhs.created).append(notificationMessage, rhs.notificationMessage).append(notificationMeta, rhs.notificationMeta).append(notificationType, rhs.notificationType).append(repeat, rhs.repeat).append(requirements, rhs.requirements).append(subscriptionName, rhs.subscriptionName).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
