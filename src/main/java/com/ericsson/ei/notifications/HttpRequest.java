/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.notifications;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.ericsson.ei.controller.model.AuthenticationType;
import com.ericsson.ei.encryption.EncryptionFormatter;
import com.ericsson.ei.encryption.Encryptor;
import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.utils.SpringContext;
import com.ericsson.ei.utils.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class HttpRequest {

    @Component
    static class HttpRequestFactory {
        // Used to enable mocking of this class in tests.
        HttpRequest createHttpRequest() {
            return new HttpRequest();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    private SubscriptionField subscriptionField;

    // Manual wired
    private HttpRequestSender httpRequestSender = SpringContext.getBean(HttpRequestSender.class);
    private JenkinsCrumb jenkinsCrumb = SpringContext.getBean(JenkinsCrumb.class);
    private UrlParser urlParser = SpringContext.getBean(UrlParser.class);
    private Encryptor encryptor = SpringContext.getBean(Encryptor.class);

    @Getter
    @Setter
    private String aggregatedObject;

    @Getter
    @Setter
    private MultiValueMap<String, String> mapNotificationMessage;

    @Getter
    @Setter
    private JsonNode subscriptionJson;

    @Getter
    @Setter
    private String url;

    @Getter
    private HttpEntity<?> request;
    @Getter
    private String contentType;
    @Getter
    private HttpHeaders headers;

    /**
     * Perform a HTTP request to a specific url. Returns the response.
     *
     * @throws AuthenticationException, HttpClientErrorException, HttpServerErrorException,
     *                                  Exception
     */
    public void perform()
            throws AuthenticationException, HttpClientErrorException, HttpServerErrorException,
            Exception {
        httpRequestSender.postDataMultiValue(this.url, this.request);
    }

    /**
     * Builds a HTTP request with headers.
     */
    public HttpRequest build() throws AuthenticationException {
        this.subscriptionField = new SubscriptionField(this.subscriptionJson);
        prepareHeaders();
        createRequest();
        this.url = urlParser.runJmesPathOnParameters(this.url, this.aggregatedObject);

        return this;
    }

    /**
     * Prepares headers to be used in a POST request. POST.
     *
     * @throws AuthenticationException
     */
    private void prepareHeaders() throws AuthenticationException {
        this.headers = new HttpHeaders();
        setContentTypeInHeader();
        addAuthenticationData();
    }

    /**
     * Creates a HTTP request based on the content type.
     *
     */
    private void createRequest() {
        boolean isApplicationXWwwFormUrlEncoded = MediaType.valueOf(contentType)
                                                           .equals(MediaType.APPLICATION_FORM_URLENCODED);
        if (isApplicationXWwwFormUrlEncoded) {
            request = new HttpEntity<MultiValueMap<String, String>>(
                    this.mapNotificationMessage, this.headers);
        } else {
            request = new HttpEntity<String>(
                    String.valueOf((mapNotificationMessage.get("")).get(0)),
                    this.headers);
        }
    }

    /**
     * Adds content type to the headers.
     */
    private void setContentTypeInHeader() {
        this.contentType = subscriptionField.get("restPostBodyMediaType");

        this.headers.setContentType(MediaType.valueOf(contentType));
        LOGGER.debug("Successfully added header: 'restPostBodyMediaType':'{}'",
                this.contentType);
    }

    /**
     * Adds the authentication details to the headers.
     *
     * @throws AuthenticationException
     */
    private void addAuthenticationData() throws AuthenticationException {
        String authType = subscriptionField.get("authenticationType");
        String username = subscriptionField.get("userName");
        String password = subscriptionField.get("password");

        boolean authenticationDetailsProvided = EncryptionFormatter.verifyAuthenticationDetails(
                authType, username, password);
        if (!authenticationDetailsProvided) {
            return;
        }
        if (encryptor != null && encryptor.isJasyptPasswordSet()
                && EncryptionFormatter.isEncrypted(password)) {
            password = doDecryption(password);
        }

        String encoding = Base64.getEncoder()
                                .encodeToString((username + ":" + password).getBytes());
        this.headers.add("Authorization", "Basic " + encoding);
        LOGGER.debug("Successfully added header for 'Authorization'");

        if (authType.equals(AuthenticationType.BASIC_AUTH_JENKINS_CSRF.getValue())) {
            JsonNode crumb = jenkinsCrumb.fetchJenkinsCrumb(encoding, this.url);
            addJenkinsCrumbData(crumb);
        }

    }

    private String doDecryption(String password) {
        return encryptor.decrypt(password);
    }

    /**
     * Adds crumb to the headers if applicable.
     *
     * @param crumb
     */
    private void addJenkinsCrumbData(JsonNode crumb) {
        if (crumb != null) {
            String crumbKey = crumb.get("crumbRequestField").asText();
            String crumbValue = crumb.get("crumb").asText();
            this.headers.add(crumbKey, crumbValue);
            LOGGER.info("Successfully added header: " + String.format("'%s':'%s'", crumbKey,
                    crumbValue));
        }
    }
}
