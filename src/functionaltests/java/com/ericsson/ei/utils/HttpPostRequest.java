package com.ericsson.ei.utils;

import org.apache.http.client.methods.*;
import org.springframework.http.ResponseEntity;

public class HttpPostRequest extends HttpRequest {

    /**
     * Build together a httpPost object
     */
    public ResponseEntity<String> build() {
        return makeRequest(new HttpPost(url + port + endpoint));
    }
}