package com.ericsson.ei.utils;

import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;


public class HttpDeleteRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDeleteRequest.class);
    @Getter private int port;
    @Getter private String url;
    @Getter private String endpoint;
    private ObjectMapper mapper = new ObjectMapper();
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    /**
     * Constructor for initializing a HttpPostRequest object
     *
     * */
    public HttpDeleteRequest() {

    }

    public HttpDeleteRequest setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpDeleteRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpDeleteRequest setEndpoint(String endpoint){
        this.endpoint = endpoint;
        return this;
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
