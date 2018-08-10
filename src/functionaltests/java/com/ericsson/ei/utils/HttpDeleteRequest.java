package com.ericsson.ei.utils;

import org.apache.http.client.methods.HttpDelete;
import org.springframework.http.ResponseEntity;

public class HttpDeleteRequest extends HttpRequest {

    /**
     * Build together a httpDelete object
     */
    public ResponseEntity<String> build() {
        return makeRequest(new HttpDelete(url + port + endpoint));
    }
}