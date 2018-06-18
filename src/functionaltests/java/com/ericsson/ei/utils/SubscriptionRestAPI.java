package com.ericsson.ei.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SubscriptionRestAPI {

    private CloseableHttpClient client = HttpClientBuilder.create().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRestAPI.class);

    /**
     * Handle the response from a HTTP request
     * @param request
     *      A HTTP request method, e.g. httpGet, httpPost
     * @return ResponseEntity
     *      containing the json content of the http response and status code from request
     * */
    public ResponseEntity<String> getResponse(HttpRequestBase request) {
        int statusCode = HttpStatus.PROCESSING.value();
        String jsonContent = "";

        try(CloseableHttpResponse httpResponse = client.execute(request)) {
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

            for (String line = bufReader.readLine(); line != null; line = bufReader.readLine()) {
                jsonContent += line;
            }
            if (jsonContent.isEmpty()) {
                jsonContent = "[]";
            }
            bufReader.close();
            inStream.close();
            statusCode = httpResponse.getStatusLine().getStatusCode();
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new ResponseEntity<>(jsonContent, HttpStatus.valueOf(statusCode));
    }
}
