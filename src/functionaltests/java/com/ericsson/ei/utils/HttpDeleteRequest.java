package com.ericsson.ei.utils;

import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;


@Accessors(chain = true)
public class HttpDeleteRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDeleteRequest.class);
    @Getter @Setter private int port;
    @Getter @Setter private String url;
    @Getter @Setter private String endpoint;
    private ObjectMapper mapper = new ObjectMapper();
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


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
