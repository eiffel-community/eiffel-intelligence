package com.ericsson.ei.utils;

import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;


public class HttpDeleteRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDeleteRequests.class);
    private int port;
    private String url = "http://localhost:";
    private String endpoint = "/subscriptions";
    private ObjectMapper mapper = new ObjectMapper();
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();

    public HttpDeleteRequests(int applicationPort) {
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
    public HttpDeleteRequests(int applicationPort, String requestUrl, String urlEndpoint) {
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
     * Build together a httpDelete object
     * */
    public SubscriptionResponse build() {
        ResponseEntity<String> response = null;
        SubscriptionResponse subscriptionResponse = null;
        HttpDelete httpDelete = new HttpDelete(url + port + endpoint);
        response = restApi.getResponse(httpDelete);

        try {
            subscriptionResponse = mapper.readValue(response.getBody().toString(), SubscriptionResponse.class);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return subscriptionResponse;
    }
}
