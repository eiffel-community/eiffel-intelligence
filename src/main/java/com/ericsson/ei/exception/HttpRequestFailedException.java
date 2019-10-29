package com.ericsson.ei.exception;

public class HttpRequestFailedException extends Exception {
    private static final long serialVersionUID = 1L;

    public HttpRequestFailedException(final String message, final Throwable e) {
        super(message, e);
    }

    public HttpRequestFailedException(final String message) {
        super(message);
    }

    public HttpRequestFailedException(final Throwable e) {
        super(e);
    }
}
