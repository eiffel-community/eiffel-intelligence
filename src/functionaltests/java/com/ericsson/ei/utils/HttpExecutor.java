package com.ericsson.ei.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;


public class HttpExecutor {

    private static HttpExecutor instance;
    private CloseableHttpClient client = HttpClientBuilder.create().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpExecutor.class);

    private HttpExecutor() {
        
    }
    
    public static HttpExecutor getInstance() {
        if(instance == null) {
            instance = new HttpExecutor();
        }
        
        return instance;
    }
    /**
     * Handle the response from a HTTP request
     * @param request
     *      A HTTP request method, e.g. httpGet, httpPost
     * @return ResponseEntity
     *      containing the json content of the http response and status code from request
     * */
    public ResponseEntity<String> executeRequest(HttpRequestBase request) {
        int statusCode = HttpStatus.PROCESSING.value();
        String jsonContent = "";

        try(CloseableHttpResponse httpResponse = client.execute(request)) {
            if(httpResponse.getEntity() != null) {
                jsonContent = StringUtils.defaultIfBlank(EntityUtils.toString(httpResponse.getEntity(), "utf-8"), "");
            }
            statusCode = httpResponse.getStatusLine().getStatusCode();
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new ResponseEntity<>(jsonContent, HttpStatus.valueOf(statusCode));
    }
}