package com.ericsson.ei.utils;

import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.*;


public class HttpRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequests.class);
    private int port;
    private CloseableHttpClient client = HttpClientBuilder.create().build();
    static JSONArray jsonParams = null;
    private ObjectMapper mapper = new ObjectMapper();


    public HttpRequests(int applicationPort) {
        port = applicationPort;
    }

    /**
     * Create a HTTP GET request
     *
     * @param url
     *
     * */
    public ResponseEntity<String> makeHttpGetRequest(String url) {
        ResponseEntity<String> response = null;

        HttpGet httpGet = new HttpGet(url);
        response = getResponse(httpGet);

        return response;
    }


    /**
     * Create a POST request
     *
     * @param url
     *      Where to send request
     * @param filePath
     *      Location to json file containing subscription
     * */
    public ResponseEntity<String> makeHttpPostRequest(String url, String filePath) {
        ResponseEntity<String> response = null;
        String fileContent = "";

        try {
            fileContent = FileUtils.readFileToString(new File(filePath), "UTF-8");
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("ERROR: " + e.getMessage());
        }

        try {
            jsonParams = new JSONArray(fileContent);
        } catch(JSONException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("ERROR: " + e.getMessage());
        }

        HttpPost httpPost = new HttpPost(url);

        // add request parameters
        StringEntity params = new StringEntity(jsonParams.toString(), "UTF-8");
        httpPost.setEntity(params);

        // add headers
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        httpPost.addHeader("Accept", "application/json");

        response = getResponse(httpPost);

        return response;
    }

    /**
     * Delete a given subscription
     *
     * The url should include the name of subscription which should be deleted
     * */
    public SubscriptionResponse makeHttpDeleteRequest(String url) {
        ResponseEntity<String> response = null;
        SubscriptionResponse subscriptionResponse = null;

        HttpDelete httpDelete = new HttpDelete(url);
        response = getResponse(httpDelete);

        try {
            subscriptionResponse = mapper.readValue(response.getBody().toString(), SubscriptionResponse.class);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return subscriptionResponse;
    }


    /**
     * Handle the response from a HTTP request
     * @param request
     *      A HTTP request method, e.g. httpGet, httpPost
     *
     * @return ResponseEntity
     *      containing the json content of the http response and statuscode from request
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
            System.out.println("ERROR: " + e.getMessage());
        }

        return new ResponseEntity<>(jsonContent, HttpStatus.valueOf(statusCode));
    }
}
