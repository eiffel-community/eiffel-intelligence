package com.ericsson.ei.exception;

public class NotificationFailureException extends Exception {

    private static final long serialVersionUID = 2L;

    public NotificationFailureException() {
        super();
    }

    public NotificationFailureException(String message) {
        super(message);
    }

    public NotificationFailureException(String message, Throwable e) {
        super(message, e);
    }
}
