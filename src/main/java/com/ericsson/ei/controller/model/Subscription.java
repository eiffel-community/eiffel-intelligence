
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "created",
    "notificationMessage",
    "notificationMeta",
    "notificationType",
    "repeat",
    "requirements",
    "subscriptionName",
    "timeout"
})
public class Subscription {

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
    @JsonProperty("timeout")
    private Integer timeout;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The created
     */
    @JsonProperty("created")
    public String getCreated() {
        return created;
    }

    /**
     * 
     * @param created
     *     The created
     */
    @JsonProperty("created")
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * 
     * @return
     *     The notificationMessage
     */
    @JsonProperty("notificationMessage")
    public String getNotificationMessage() {
        return notificationMessage;
    }

    /**
     * 
     * @param notificationMessage
     *     The notificationMessage
     */
    @JsonProperty("notificationMessage")
    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    /**
     * 
     * @return
     *     The notificationMeta
     */
    @JsonProperty("notificationMeta")
    public String getNotificationMeta() {
        return notificationMeta;
    }

    /**
     * 
     * @param notificationMeta
     *     The notificationMeta
     */
    @JsonProperty("notificationMeta")
    public void setNotificationMeta(String notificationMeta) {
        this.notificationMeta = notificationMeta;
    }

    /**
     * 
     * @return
     *     The notificationType
     */
    @JsonProperty("notificationType")
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * 
     * @param notificationType
     *     The notificationType
     */
    @JsonProperty("notificationType")
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * 
     * @return
     *     The repeat
     */
    @JsonProperty("repeat")
    public Boolean getRepeat() {
        return repeat;
    }

    /**
     * 
     * @param repeat
     *     The repeat
     */
    @JsonProperty("repeat")
    public void setRepeat(Boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * 
     * @return
     *     The requirements
     */
    @JsonProperty("requirements")
    public List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * 
     * @param requirements
     *     The requirements
     */
    @JsonProperty("requirements")
    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    /**
     * 
     * @return
     *     The subscriptionName
     */
    @JsonProperty("subscriptionName")
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * 
     * @param subscriptionName
     *     The subscriptionName
     */
    @JsonProperty("subscriptionName")
    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    /**
     * 
     * @return
     *     The timeout
     */
    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * 
     * @param timeout
     *     The timeout
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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
        return new HashCodeBuilder().append(created).append(notificationMessage).append(notificationMeta).append(notificationType).append(repeat).append(requirements).append(subscriptionName).append(timeout).append(additionalProperties).toHashCode();
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
        return new EqualsBuilder().append(created, rhs.created).append(notificationMessage, rhs.notificationMessage).append(notificationMeta, rhs.notificationMeta).append(notificationType, rhs.notificationType).append(repeat, rhs.repeat).append(requirements, rhs.requirements).append(subscriptionName, rhs.subscriptionName).append(timeout, rhs.timeout).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
