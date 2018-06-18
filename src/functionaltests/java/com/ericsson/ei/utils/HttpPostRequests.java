package com.ericsson.ei.utils;

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


public class HttpPostRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPostRequests.class);
    private int port;
    private String url = "http://localhost:";
    private String endpoint = "/subscriptions";
    private Map<String, String> headers = new HashMap<>();
    private StringEntity params;

    private static JSONArray jsonParams = null;
    private SubscriptionRestAPI restApi = new SubscriptionRestAPI();


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
    public HttpPostRequests(int applicationPort, String requestUrl, String urlEndpoint) {
        port = applicationPort;
        endpoint = urlEndpoint;
        url = requestUrl;
    }

    /**
     * Constructor for initializing a HttpPostRequest object
     *
     * @param applicationPort
     *      which port to use for request
     * @param requestUrl
     *      which url to use for the request
     * @param urlEndpoint
     *      which endpoint to use
     * @param requestHeaders
     *      a map of the request headers
     * @param filePath
     *      path to file containing json for request body
     * */
    public HttpPostRequests(int applicationPort, String requestUrl, String urlEndpoint, Map requestHeaders, String filePath) {
        port = applicationPort;
        url = requestUrl;
        endpoint = urlEndpoint;
        headers = requestHeaders;
        setParams(filePath);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int applicationPort) {
        port = applicationPort;
    }

    public String getUrl() {
        return url + port + endpoint;
    }

    public void setUrl(String requestUrl) {
        url = requestUrl;
    }

    public void setUrl(String requestUrl, String endpoint) {
        url = requestUrl + port + endpoint;
    }

    public Map getHeaders() {
        return headers;
    }

    public void setHeaders(Map requestHeaders) {
        headers = requestHeaders;
    }

    public StringEntity getParams() {
        return params;
    }

    public void setParams(String filePath) {
        String fileContent = "";

        try {
            fileContent = FileUtils.readFileToString(new File(filePath), "UTF-8");
            jsonParams = new JSONArray(fileContent);
        } catch(IOException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        params = new StringEntity(jsonParams.toString(), "UTF-8");
    }

    /**
     * Build together a httpPost object
     * */
    public ResponseEntity<String> build() {
        ResponseEntity<String> response = null;
        HttpPost httpPost = new HttpPost(url + port + endpoint);

        // set params on httpPost
        httpPost.setEntity(params);

        // add headers if they exist
        if (!headers.isEmpty()) {
            for(Map.Entry<String, String> e : headers.entrySet()) {
                httpPost.addHeader(e.getKey(), e.getValue());
            }
        }
        response = restApi.getResponse(httpPost);
        return response;
    }

}
