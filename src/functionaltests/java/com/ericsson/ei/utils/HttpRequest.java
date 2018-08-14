package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class HttpRequest {

    private HttpRequestBase request;
    private HttpExecutor executor = HttpExecutor.getInstance();

    public enum HttpMethod {
        GET, POST, DELETE, PUT
    }

    @Getter
    @Setter
    protected int port;
    @Getter
    @Setter
    protected String host;
    @Getter
    @Setter
    protected String endpoint;
    @Getter
    protected Map<String, String> params = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(HttpMethod method) {
        switch (method) {
        case POST:
            request = new HttpPost();
            break;
        case GET:
            request = new HttpGet();
            break;
        case DELETE:
            request = new HttpDelete();
            break;
        case PUT:
            request = new HttpPut();
            break;
        }
    }

    public HttpRequest setHeaders(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    public HttpRequest setParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public HttpRequest setBody(String body) {
        ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, "UTF-8"));
        return this;
    }

    public void setBody(File file) {
        String fileContent = "";
        try {
            fileContent = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        setBody(fileContent);
    }

    public ResponseEntity<String> performRequest() throws URISyntaxException {
        URIBuilder builder = new URIBuilder("http://" + host + ":" + port + endpoint);
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        request.setURI(builder.build());
        return executor.executeRequest(request);
    }
}