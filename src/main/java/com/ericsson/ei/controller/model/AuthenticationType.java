package com.ericsson.ei.controller.model;

public enum AuthenticationType {
    NO_AUTH("NO_AUTH"), BASIC_AUTH("BASIC_AUTH"),
    BASIC_AUTH_JENKINS_CSRF("BASIC_AUTH_JENKINS_CSRF");

    private final String authenticationType;

    private AuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getValue() {
        return this.authenticationType;
    }
}