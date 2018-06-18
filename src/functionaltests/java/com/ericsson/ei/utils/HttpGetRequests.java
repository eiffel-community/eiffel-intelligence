package com.ericsson.ei.utils;

import org.apache.http.client.methods.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


public class HttpGetRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGetRequests.class);
    private int port;
    private String url = "http://localhost:";
    private String endpoint = "/subscriptions";
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    public HttpGetRequests(int applicationPort) {
        port = applicationPort;
    }

    /**
     * Constructor for initializing a HttpPostRequest object
     *
     * @param applicationPort
     *      which port to use for request
     * @param requestUrl
     *      url for request
     * @param urlEndpoint
     *      which endpoint to use
     * */
    public HttpGetRequests(int applicationPort, String requestUrl, String urlEndpoint) {
        port = applicationPort;
        endpoint = urlEndpoint;
        url = requestUrl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int applicationPort) {
        port = applicationPort;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String requestUrl) {
        url = requestUrl;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String urlEndpoint) {
        endpoint = urlEndpoint;
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
