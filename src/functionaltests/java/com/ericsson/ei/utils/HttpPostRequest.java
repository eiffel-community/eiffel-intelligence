package com.ericsson.ei.utils;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class HttpPostRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPostRequest.class);
    @Getter private int port;
    @Getter private String url;
    @Getter private String endpoint;
    @Getter private Map<String, String> headers = new HashMap<>();
    @Getter private StringEntity params;

    private static JSONArray jsonParams = null;
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    /**
     * Constructor for initializing a HttpPostRequest object
     */
    public HttpPostRequest() {

    }

    public HttpPostRequest setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpPostRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpPostRequest setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public HttpPostRequest setHeaders(String headerKey, String headerValue) {
        this.headers.put(headerKey, headerValue);
        return this;
    }

    public HttpPostRequest setParams(String filePath) {
        String fileContent = "";

        try {
            fileContent = FileUtils.readFileToString(new File(filePath), "UTF-8");
            jsonParams = new JSONArray(fileContent);
        } catch(IOException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        params = new StringEntity(jsonParams.toString(), "UTF-8");
        return this;
    }

    /**
     * Build together a httpPost object
     * */
    public ResponseEntity<String> build() {
        ResponseEntity<String> response = null;
        HttpPost httpPost = new HttpPost(url + port + endpoint);

        // set params on httpPost
        httpPost.setEntity(params);

        // add headers if they exist
        if (!headers.isEmpty()) {
            for(Map.Entry<String, String> e : headers.entrySet()) {
                httpPost.addHeader(e.getKey(), e.getValue());
            }
        }
        response = restApi.getResponse(httpPost);
        return response;
    }

}
