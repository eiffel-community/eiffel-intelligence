
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
    "aggregationtype",
    "created",
    "notificationMeta",
    "notificationType",
    "restPostBodyMediaType",
    "notificationMessageKeyValues",
    "repeat",
    "requirements",
    "subscriptionName",
    "authenticationType",
    "userName",
    "password",
    "ldapUserName"
})
public class Subscription {

    @JsonProperty("aggregationtype")
    private String aggregationtype;
    @JsonProperty("created")
    private Object created;
    @JsonProperty("notificationMeta")
    private String notificationMeta;
    @JsonProperty("notificationType")
    private String notificationType;
    @JsonProperty("restPostBodyMediaType")
    private String restPostBodyMediaType;
    @JsonProperty("notificationMessageKeyValues")
    @Valid
    private List<NotificationMessageKeyValue> notificationMessageKeyValues = new ArrayList<NotificationMessageKeyValue>();
    @JsonProperty("repeat")
    private Boolean repeat;
    @JsonProperty("requirements")
    @Valid
    private List<Requirement> requirements = new ArrayList<Requirement>();
    @JsonProperty("subscriptionName")
    private String subscriptionName;
    @JsonProperty("authenticationType")
    private String authenticationType;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("password")
    private String password;
    @JsonProperty("ldapUserName")
    private String ldapUserName;
    @JsonIgnore
    @Valid
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
    public Object getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(Object created) {
        this.created = created;
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

    @JsonProperty("restPostBodyMediaType")
    public String getRestPostBodyMediaType() {
        return restPostBodyMediaType;
    }

    @JsonProperty("restPostBodyMediaType")
    public void setRestPostBodyMediaType(String restPostBodyMediaType) {
        this.restPostBodyMediaType = restPostBodyMediaType;
    }

    @JsonProperty("notificationMessageKeyValues")
    public List<NotificationMessageKeyValue> getNotificationMessageKeyValues() {
        return notificationMessageKeyValues;
    }

    @JsonProperty("notificationMessageKeyValues")
    public void setNotificationMessageKeyValues(List<NotificationMessageKeyValue> notificationMessageKeyValues) {
        this.notificationMessageKeyValues = notificationMessageKeyValues;
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

    @JsonProperty("authenticationType")
    public String getAuthenticationType() {
        return authenticationType;
    }

    @JsonProperty("authenticationType")
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    @JsonProperty("userName")
    public String getUserName() {
        return userName;
    }

    @JsonProperty("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("ldapUserName")
    public String getLdapUserName() {
        return ldapUserName;
    }

    @JsonProperty("ldapUserName")
    public void setLdapUserName(String ldapUserName) {
        this.ldapUserName = ldapUserName;
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
        return new ToStringBuilder(this).append("aggregationtype", aggregationtype).append("created", created).append("notificationMeta", notificationMeta).append("notificationType", notificationType).append("restPostBodyMediaType", restPostBodyMediaType).append("notificationMessageKeyValues", notificationMessageKeyValues).append("repeat", repeat).append("requirements", requirements).append("subscriptionName", subscriptionName).append("authenticationType", authenticationType).append("userName", userName).append("password", password).append("ldapUserName", ldapUserName).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(requirements).append(created).append(restPostBodyMediaType).append(notificationType).append(userName).append(aggregationtype).append(notificationMessageKeyValues).append(password).append(repeat).append(subscriptionName).append(notificationMeta).append(authenticationType).append(additionalProperties).append(ldapUserName).toHashCode();
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
        return new EqualsBuilder().append(requirements, rhs.requirements).append(created, rhs.created).append(restPostBodyMediaType, rhs.restPostBodyMediaType).append(notificationType, rhs.notificationType).append(userName, rhs.userName).append(aggregationtype, rhs.aggregationtype).append(notificationMessageKeyValues, rhs.notificationMessageKeyValues).append(password, rhs.password).append(repeat, rhs.repeat).append(subscriptionName, rhs.subscriptionName).append(notificationMeta, rhs.notificationMeta).append(authenticationType, rhs.authenticationType).append(additionalProperties, rhs.additionalProperties).append(ldapUserName, rhs.ldapUserName).isEquals();
    }

}
