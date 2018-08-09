package com.ericsson.ei.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
public class HttpPostRequest extends HttpRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPostRequest.class);
    @Getter
    @Setter
    private int port;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String endpoint;
    @Getter
    private Map<String, String> headers = new HashMap<>();
    @Getter
    private StringEntity params;

    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();

    public HttpPostRequest setHeaders(String headerKey, String headerValue) {
        this.headers.put(headerKey, headerValue);
        return this;
    }

    public HttpPostRequest setBody(String body) {
        params = new StringEntity(body, "UTF-8");
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

    /**
     * Build together a httpPost object
     */
    public ResponseEntity<String> build() {
        HttpPost httpPost = new HttpPost(url + port + endpoint);
        httpPost.setEntity(params);
        
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpPost.addHeader(e.getKey(), e.getValue());
            }
        }
        return restApi.getResponse(httpPost);
    }

}
