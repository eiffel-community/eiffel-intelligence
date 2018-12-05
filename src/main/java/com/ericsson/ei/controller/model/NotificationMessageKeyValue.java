
package com.ericsson.ei.controller.model;

import java.util.HashMap;
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
    "formkey",
    "formvalue"
})
public class NotificationMessageKeyValue {

    @JsonProperty("formkey")
    private String formkey;
    @JsonProperty("formvalue")
    private String formvalue;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("formkey")
    public String getFormkey() {
        return formkey;
    }

    @JsonProperty("formkey")
    public void setFormkey(String formkey) {
        this.formkey = formkey;
    }

    @JsonProperty("formvalue")
    public String getFormvalue() {
        return formvalue;
    }

    @JsonProperty("formvalue")
    public void setFormvalue(String formvalue) {
        this.formvalue = formvalue;
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
        return new ToStringBuilder(this).append("formkey", formkey).append("formvalue", formvalue).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(formkey).append(formvalue).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationMessageKeyValue) == false) {
            return false;
        }
        NotificationMessageKeyValue rhs = ((NotificationMessageKeyValue) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(formkey, rhs.formkey).append(formvalue, rhs.formvalue).isEquals();
    }

}
