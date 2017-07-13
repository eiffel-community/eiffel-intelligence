package com.ericsson.ei.exception;

public class SubscriptionNotFoundException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public SubscriptionNotFoundException() {
        super();
    }
    
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
    
    public SubscriptionNotFoundException(String message, Throwable e) {
        super(message, e);
    }
    
}
