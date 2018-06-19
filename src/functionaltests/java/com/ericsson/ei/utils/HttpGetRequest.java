package com.ericsson.ei.utils;

import lombok.Getter;
import org.apache.http.client.methods.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


public class HttpGetRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGetRequest.class);
    @Getter private int port;
    @Getter private String url;
    @Getter private String endpoint;
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    /**
     * Constructor for initializing a HttpPostRequest object
     * */
    public HttpGetRequest() {

    }

    public HttpGetRequest setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpGetRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpGetRequest setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Build together a httpGet object
     * */
    public ResponseEntity<String> build() {
        ResponseEntity<String> response = null;
        HttpGet httpGet = new HttpGet(url + port + endpoint);

        response = restApi.getResponse(httpGet);
        return response;
    }

}
