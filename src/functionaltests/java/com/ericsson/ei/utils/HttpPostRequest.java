package com.ericsson.ei.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


@Accessors(chain = true)
public class HttpPostRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPostRequest.class);
    @Getter @Setter private int port;
    @Getter @Setter private String url;
    @Getter @Setter private String endpoint;
    @Getter private Map<String, String> headers = new HashMap<>();
    @Getter private StringEntity params;

    private static JSONArray jsonParams = null;
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


    public HttpPostRequest setHeaders(String headerKey, String headerValue) {
        this.headers.put(headerKey, headerValue);
        return this;
    }

    public HttpPostRequest setParams(String filePath) {
        String fileContent = "";

        try {
            fileContent = FileUtils.readFileToString(new File(filePath), "UTF-8");
            jsonParams = new JSONArray(fileContent);
        } catch(IOException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        params = new StringEntity(jsonParams.toString(), "UTF-8");
        return this;
    }

    /**
     * Build together a httpPost object
     * */
    public ResponseEntity<String> build() {
        HttpPost httpPost = new HttpPost(url + port + endpoint);

        // set params on httpPost
        httpPost.setEntity(params);

        // add headers if they exist
        if (!headers.isEmpty()) {
            for(Map.Entry<String, String> e : headers.entrySet()) {
                httpPost.addHeader(e.getKey(), e.getValue());
            }
        }
        return restApi.getResponse(httpPost);
    }

}
