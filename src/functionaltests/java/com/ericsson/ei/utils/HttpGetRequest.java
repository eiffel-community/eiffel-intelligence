package com.ericsson.ei.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.http.client.methods.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


@Accessors(chain = true)
public class HttpGetRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGetRequest.class);

    @Getter @Setter private int port;
    @Getter @Setter private String url;
    @Getter @Setter private String endpoint;
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    /**
     * Build together a httpGet object
     * */
    public ResponseEntity<String> build() {
        HttpGet httpGet = new HttpGet(url + port + endpoint);

        return restApi.getResponse(httpGet);
    }

}
