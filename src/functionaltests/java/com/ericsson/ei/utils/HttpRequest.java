package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class HttpRequest {

    @Getter
    @Setter
    protected int port;
    @Getter
    @Setter
    protected String url;
    @Getter
    @Setter
    protected String endpoint;
    @Getter
    protected Map<String, String> headers = new HashMap<>();
    @Getter
    protected StringEntity body;

    private SubscriptionRestAPI restApi = SubscriptionRestAPI.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest setHeaders(String headerKey, String headerValue) {
        this.headers.put(headerKey, headerValue);
        return this;
    }

    public HttpRequest setBody(String body) {
        this.body = new StringEntity(body, "UTF-8");
        return this;
    }

    public void setBody(File file) {
        String fileContent = "";

        try {
            fileContent = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        setBody(fileContent);
    }
    
    public ResponseEntity<String> makeRequest(HttpRequestBase request) {
        if (body != null) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(body);
        }
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        return restApi.getResponse(request);
    }
    
    public abstract ResponseEntity<String> build();
}