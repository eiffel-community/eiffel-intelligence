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

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.ei.exception.AuthenticationException;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class JenkinsCrumb {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsCrumb.class);
    private static final String JENKINS_CRUMB_ENDPOINT = "/crumbIssuer/api/json";

    @Autowired
    private UrlParser urlParser;

    @Autowired
    private HttpRequestSender httpRequestSender;

    /**
     * Tries to fetch a Jenkins crumb. Will return Jenkins crumb data in JSON
     * format, or null if no crumb was found.
     *
     * @param encoding
     * @return JenkinsJsonCrumbData
     * @throws AuthenticationException
     */
    public JsonNode fetchJenkinsCrumb(String encoding, String url)
            throws AuthenticationException {
        try {
            URL crumbUrl = buildJenkinsCrumbUrl(url);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + encoding);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<JsonNode> response = httpRequestSender.makeGetRequest(crumbUrl.toString(),
                    headers);

            JsonNode JenkinsJsonCrumbData = response.getBody();
            return JenkinsJsonCrumbData;

        } catch (MalformedURLException e) {
            String message = "Failed to format url to collect jenkins crumb.";
            LOGGER.error(message, e);
            throw new AuthenticationException(message, e);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED == e.getStatusCode()) {
                String message = "Failed to fetch crumb. Authentication failed, wrong username or password.";
                LOGGER.error(message, e);
                throw new AuthenticationException(message, e);
            }
            if (HttpStatus.NOT_FOUND == e.getStatusCode()) {
                String message = String.format(
                    "Failed to fetch crumb. The authentication type is BASIC_AUTH_JENKINS_CSRF,"
                        + " but CSRF Protection seems disabled in Jenkins.");
                LOGGER.warn(message, e);
                return null;
            }
            throw e;
        }
    }

    /**
     * Replaces the user given context paths with the crumb issuer context path.
     *
     * @param url
     * @return jenkinsCrumbUrl
     * @throws MalformedURLException
     */
    private URL buildJenkinsCrumbUrl(String url) throws MalformedURLException {
        String baseUrl = urlParser.extractBaseUrl(url);
        URL jenkinsCrumbUrl = new URL(baseUrl + JENKINS_CRUMB_ENDPOINT);
        return jenkinsCrumbUrl;
    }
}