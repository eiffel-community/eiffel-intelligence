package com.ericsson.ei.utils;

import org.apache.http.client.methods.*;
import org.springframework.http.ResponseEntity;

public class HttpGetRequest extends HttpRequest {

    /**
     * Build together a httpGet object
     */
    public ResponseEntity<String> build() {
        return doRequest(new HttpGet(url + port + endpoint));
    }
}