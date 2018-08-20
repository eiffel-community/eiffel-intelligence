package com.ericsson.ei.utils;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;


public final class HttpExecutor {

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
     * Close existing HttpClient and create a new one.
     *
     * @throws IOException
     */
    public void recreateHttpClient() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (IOException e) {
                LOGGER.error("Error: {}", e.getMessage());
            }
        }
        this.client = HttpClientBuilder.create().build();
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
        Header[] headers = null;

        try(CloseableHttpResponse httpResponse = client.execute(request)) {
            if(httpResponse.getEntity() != null) {
                jsonContent = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
            }
            statusCode = httpResponse.getStatusLine().getStatusCode();
            headers = httpResponse.getAllHeaders();
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            headersMap.add(header.getName(), header.getValue());
        }
        return new ResponseEntity<>(jsonContent, headersMap, HttpStatus.valueOf(statusCode));
    }
}