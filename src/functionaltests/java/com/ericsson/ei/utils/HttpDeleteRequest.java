package com.ericsson.ei.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.http.client.methods.HttpDelete;
import org.springframework.http.ResponseEntity;

@Accessors(chain = true)
public class HttpDeleteRequest {

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private String endpoint;

    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();

    /**
     * Build together a httpDelete object
     */
    public ResponseEntity<String> build() {
        HttpDelete httpDelete = new HttpDelete(url + port + endpoint);

        return restApi.getResponse(httpDelete);
    }
}