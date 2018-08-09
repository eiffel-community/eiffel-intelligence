package com.ericsson.ei.utils;

import java.io.File;

import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class HttpRequest {
    @Getter
    @Setter
    private int port;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String endpoint;

    public HttpPostRequest setHeaders(String string, String string2) {
        return null;
    }
    
    public HttpPostRequest setBody(String string) {
        return null;
    }
    
    public void setBody(File file) {
    }
    
    public abstract ResponseEntity<String> build();
}