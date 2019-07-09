/*
   Copyright 2017 Ericsson AB.
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
package com.ericsson.ei.subscription;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.handlers.DateUtils;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import lombok.Getter;

/**
 * Represents the REST POST notification mechanism and the alternate way to save the
 * aggregatedObject details in the database when the notification fails.
 *
 * @author xjibbal
 */

@Component
public class InformSubscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformSubscriber.class);
    // Regular expression for replacing unexpected character like \"
    private static final String REGEX = "^\"|\"$";
    private static final String JENKINS_CRUMB_ENDPOINT = "/crumbIssuer/api/json";

    private static final String AUTHENTICATION_TYPE_NO_AUTH = "NO_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH = "BASIC_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF = "BASIC_AUTH_JENKINS_CSRF";

    @Getter
    @Value("${notification.failAttempt:#{0}}")
    private int failAttempt;

    @Getter
    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Getter
    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Getter
    @Value("${notification.ttl.value}")
    private int ttlValue;

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private HttpRequestSender httpRequestSender;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private EmailSender emailSender;

    /**
     * Extracts the mode of notification through which the subscriber should be notified, from the
     * subscription Object. And if the notification fails, then it saved in the database.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @throws AuthenticationException
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson)
            throws AuthenticationException {
        String subscriptionName = getSubscriptionField("subscriptionName", subscriptionJson);
        String notificationType = getSubscriptionField("notificationType", subscriptionJson);
        String notificationMeta = getSubscriptionField("notificationMeta", subscriptionJson);

        MultiValueMap<String, String> mapNotificationMessage = mapNotificationMessage(
                aggregatedObject, subscriptionJson);

        if (notificationType.trim().equals("REST_POST")) {
            LOGGER.debug("Notification through REST_POST");
            // Prepare notification meta
            notificationMeta = replaceParamsValuesWithAggregatedData(aggregatedObject,
                    notificationMeta);

            // Prepare request headers
            HttpHeaders headers = prepareHeaders(notificationMeta, subscriptionJson);

            // Make HTTP request(s)
            boolean success = makeHTTPRequests(notificationMeta, mapNotificationMessage, headers);

            if (!success) {
                String missedNotification = prepareMissedNotification(aggregatedObject,
                        subscriptionName, notificationMeta);
                LOGGER.debug("Prepared 'missed notification' document : {}", missedNotification);
                mongoDBHandler.createTTLIndex(missedNotificationDataBaseName,
                        missedNotificationCollectionName, "Time", ttlValue);
                saveMissedNotificationToDB(missedNotification);
            }
        }

        if (notificationType.trim().equals("MAIL")) {
            LOGGER.debug("Notification through EMAIL");
            String subject = getSubscriptionField("emailSubject", subscriptionJson);
            emailSender.sendEmail(notificationMeta, String.valueOf((mapNotificationMessage.get(""))
                                                                                                   .get(0)),
                    subject);
        }
    }

    /**
     * Attempts to make HTTP POST requests. If the request fails, it is retried until the maximum
     * number of failAttempts have been reached.
     *
     * @param notificationMeta       The URL to send the request to
     * @param mapNotificationMessage The body of the HTTP request
     * @param headers
     * @return success A boolean value depending on the outcome of the final HTTP request
     * @throws AuthenticationException
     */
    private boolean makeHTTPRequests(String notificationMeta,
            MultiValueMap<String, String> mapNotificationMessage, HttpHeaders headers)
            throws AuthenticationException {
        boolean success = false;
        int requestTries = 0;

        do {
            requestTries++;
            success = httpRequestSender.postDataMultiValue(notificationMeta, mapNotificationMessage,
                    headers);
            LOGGER.debug("After trying for {} time(s), the result is : {}", requestTries, success);
        } while (!success && requestTries <= failAttempt);

        return success;
    }

    /**
     * Prepares headers to be used when making a rest call with the method POST.
     *
     * @param notificationMeta A String containing a URL
     * @param subscriptionJson Used to extract the rest post body media type from
     * @return headers
     * @throws AuthenticationException
     */
    private HttpHeaders prepareHeaders(String notificationMeta, JsonNode subscriptionJson)
            throws AuthenticationException {
        HttpHeaders headers = new HttpHeaders();
        String headerContentMediaType = getSubscriptionField("restPostBodyMediaType",
                subscriptionJson);
        headers.setContentType(MediaType.valueOf(headerContentMediaType));
        LOGGER.debug("Successfully added header: 'restPostBodyMediaType':'{}'",
                headerContentMediaType);

        headers = addAuthenticationData(headers, notificationMeta, subscriptionJson);

        return headers;
    }

    /**
     * Adds the authentication details to the headers.
     *
     * @param headers
     * @param notificationMeta A String containing a URL
     * @param subscriptionJson Used to extract the authentication type from
     * @return headers
     * @throws AuthenticationException
     */
    private HttpHeaders addAuthenticationData(HttpHeaders headers, String notificationMeta,
            JsonNode subscriptionJson) throws AuthenticationException {
        String authType = getSubscriptionField("authenticationType", subscriptionJson);
        String username = getSubscriptionField("userName", subscriptionJson);
        String password = getSubscriptionField("password", subscriptionJson);

        boolean authenticationDetailsProvided = isAuthenticationDetailsProvided(authType, username,
                password);
        if (!authenticationDetailsProvided) {
            return headers;
        }

        String encoding = Base64.getEncoder()
                                .encodeToString((username + ":" + password)
                                                                           .getBytes());
        headers.add("Authorization", "Basic " + encoding);
        LOGGER.debug("Successfully added header for 'Authorization'");

        if (authType.equals(AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF)) {
            headers = addJenkinsCrumbData(headers, encoding, notificationMeta);
        }

        return headers;
    }

    /**
     * Returns a boolean indicating that authentication details was provided in the subscription
     *
     * @param authType
     * @param username
     * @param password
     * @return
     */
    private boolean isAuthenticationDetailsProvided(String authType, String username,
            String password) {
        if (authType.isEmpty() || authType.equals(AUTHENTICATION_TYPE_NO_AUTH)) {
            return false;
        }

        if (username.equals("") && password.equals("")) {
            LOGGER.error("userName/password field in subscription is missing.");
            return false;
        }

        return true;
    }

    /**
     * Adds crumb to the headers if applicable.
     *
     * @param headers
     * @param encoding
     * @param notificationMeta A String containing a URL
     * @return headers Headers containing Jenkins crumb data
     * @throws AuthenticationException
     */
    private HttpHeaders addJenkinsCrumbData(HttpHeaders headers, String encoding,
            String notificationMeta) throws AuthenticationException {
        LOGGER.info("Jenkins Crumb data is about to be fetched.");
        JsonNode jenkinsJsonCrumbData = fetchJenkinsCrumb(encoding, notificationMeta);
        if (jenkinsJsonCrumbData != null) {
            String crumbKey = jenkinsJsonCrumbData.get("crumbRequestField").asText();
            String crumbValue = jenkinsJsonCrumbData.get("crumb").asText();
            headers.add(crumbKey, crumbValue);
            LOGGER.info("Successfully added header: {}", String.format("'%s':'%s'", crumbKey,
                    crumbValue));
        }
        return headers;
    }

    /**
     * Tries to fetch a Jenkins crumb. Will return Jenkins crumb data in JSON format, or null if no
     * crumb was found.
     *
     * @param encoding
     * @param notificationMeta A String containing a URL
     * @return JenkinsJsonCrumbData
     * @throws AuthenticationException
     */
    private JsonNode fetchJenkinsCrumb(String encoding, String notificationMeta)
            throws AuthenticationException {
        try {
            URL url = buildJenkinsCrumbUrl(notificationMeta);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + encoding);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<JsonNode> response = httpRequestSender.makeGetRequest(url.toString(),
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
                        "Failed to fetch crumb. The authentication type is %s,"
                                + " but CSRF Protection seems disabled in Jenkins.",
                        AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF);
                LOGGER.warn(message, e);
                return null;
            }
            throw e;
        }
    }

    /**
     * Replaces the user given context paths with the crumb issuer context path.
     *
     * @param notificationMeta
     * @return
     * @throws MalformedURLException
     */
    private URL buildJenkinsCrumbUrl(String notificationMeta) throws MalformedURLException {
        String baseUrl = extractBaseUrl(notificationMeta);
        URL url = new URL(baseUrl + JENKINS_CRUMB_ENDPOINT);
        return url;
    }

    /**
     * Extracts the url parameters from the notification meta. It runs the parameter values through
     * JMESPath to replace wanted parameter values with data from the aggregated object. It then
     * reformats the notification meta containing the new parameters.
     *
     * @param aggregatedObject The aggregated object contains the url parameters which will be
     *                         updated
     * @param notificationMeta A String containing a URL
     * @return String
     */
    private String replaceParamsValuesWithAggregatedData(String aggregatedObject,
            String notificationMeta) {
        if (!notificationMeta.contains("?")) {
            return notificationMeta;
        }
        LOGGER.debug("Unformatted notificationMeta = " + notificationMeta);

        try {
            String baseUrl = extractBaseUrl(notificationMeta);
            String contextPath = extractContextPath(notificationMeta);
            List<NameValuePair> params = extractUrlParameters(notificationMeta);
            LOGGER.debug("Notification meta in parts:\n ## Base Url: "
                    + "{}\n ## Context Path: {}\n ## URL Parameters: {} ", baseUrl, contextPath,
                    params);

            List<NameValuePair> processedParams = processJMESPathParameters(aggregatedObject,
                    params);
            LOGGER.debug("JMESPath processed parameters :\n ## {}", processedParams);
            String encodedQuery = URLEncodedUtils.format(processedParams, "UTF8");

            notificationMeta = String.format("%s%s?%s", baseUrl, contextPath, encodedQuery);
            LOGGER.debug("Formatted notificationMeta = {}", notificationMeta);

            return notificationMeta;
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to extract parameters.", e);
            return notificationMeta;
        }
    }

    /**
     * Extracts the query from the notificationMeta and returns them as a list of KeyValuePair.
     *
     * @param notificationMeta
     * @return
     * @throws MalformedURLException
     */
    private List<NameValuePair> extractUrlParameters(String notificationMeta)
            throws MalformedURLException {
        URL url = new URL(notificationMeta);
        String query = url.getQuery();
        List<NameValuePair> params = splitQuery(query);
        return params;
    }

    /**
     * Splits a query string into one pair for each key and value. Loops said pairs and extracts the
     * key and value as KeyValuePair. Adds KeyValuePair to list.
     *
     * @param query
     * @return List<KeyValuePair>
     */
    private List<NameValuePair> splitQuery(String query) {
        List<NameValuePair> queryMap = new ArrayList<>();
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            NameValuePair nameValuePair = extractKeyAndValue(pair);
            queryMap.add(nameValuePair);
        }

        return queryMap;
    }

    /**
     * Extracts and decodes the key and value from a set of parameters.
     *
     * @param pair
     * @return KeyValuePair
     */
    private NameValuePair extractKeyAndValue(String pair) {
        int firstIndexOfEqualsSign = pair.indexOf("=");
        String key = "";
        String value = "";

        if (firstIndexOfEqualsSign > 0) {
            key = pair.substring(0, firstIndexOfEqualsSign);
        }

        if (pair.length() > firstIndexOfEqualsSign + 1) {
            value = pair.substring(firstIndexOfEqualsSign + 1);
        }

        return new BasicNameValuePair(key, value);
    }

    /**
     * Runs JMESPath rules on values in a list of KeyValuePair and replaces the value with extracted
     * data.
     *
     * @param aggregatedObject
     * @param params
     * @return List<NameValuePair>
     * @throws UnsupportedEncodingException
     */
    private List<NameValuePair> processJMESPathParameters(String aggregatedObject,
            List<NameValuePair> params) throws UnsupportedEncodingException {
        List<NameValuePair> processedParams = new ArrayList<>();

        for (NameValuePair param : params) {
            String name = URLDecoder.decode(param.getName(), "UTF-8");
            String value = URLDecoder.decode(param.getValue(), "UTF-8");

            LOGGER.debug("Input parameter key and value: {} : {}", name, value);
            value = jmespath.runRuleOnEvent(value.replaceAll(REGEX, ""), aggregatedObject)
                            .toString()
                            .replaceAll(REGEX, "");

            LOGGER.debug("Formatted parameter key and value: {} : {}", name, value);
            processedParams.add(new BasicNameValuePair(name, value));
        }
        return processedParams;
    }

    /**
     * Returns the base url from the notification meta.
     *
     * @param notificationMeta A String containing a URL
     * @return The base url, which excludes context path and parameters.
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String extractBaseUrl(String notificationMeta) throws MalformedURLException {
        URL url = new URL(notificationMeta);
        String protocol = url.getProtocol();
        String authority = url.getAuthority();
        return String.format("%s://%s", protocol, authority);
    }

    /**
     * Returns the context path from the notification meta.
     *
     * @param notificationMeta A String containing a URL
     * @return contextPath
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String extractContextPath(String notificationMeta) throws MalformedURLException {
        URL url = new URL(notificationMeta);
        String contextPath = url.getPath();
        return contextPath;
    }

    /**
     * Saves the missed Notification into a single document in the database.
     */
    private void saveMissedNotificationToDB(String missedNotification) {
        try {
            mongoDBHandler.insertDocument(missedNotificationDataBaseName,
                    missedNotificationCollectionName, missedNotification);
            LOGGER.debug("Notification saved in the database");
        } catch (MongoWriteException e) {
            LOGGER.debug("Failed to insert the notification into database.", e);
        }
    }

    /**
     * Prepares the document to be saved in the missed notification database.
     *
     * @param aggregatedObject
     * @param subscriptionName
     * @param notificationMeta
     * @return String
     */
    private String prepareMissedNotification(String aggregatedObject, String subscriptionName,
            String notificationMeta) {
        BasicDBObject document = new BasicDBObject();
        document.put("subscriptionName", subscriptionName);
        document.put("notificationMeta", notificationMeta);
        try {
            document.put("Time", DateUtils.getDate());
        } catch (ParseException e) {
            LOGGER.error("Failed to get date object.", e);
        }
        document.put("AggregatedObject", BasicDBObject.parse(aggregatedObject));
        return document.toString();
    }

    /**
     * Given the field name, returns its value.
     *
     * @param subscriptionJson
     * @param fieldName
     * @return field value
     */
    private String getSubscriptionField(String fieldName, JsonNode subscriptionJson) {
        String value;
        if (subscriptionJson.get(fieldName) != null) {
            value = subscriptionJson.get(fieldName).asText();
            LOGGER.debug("Extracted value [{}] from subscription field [{}].", value, fieldName);
        } else {
            value = "";
        }
        return value;
    }

    /**
     * Extracts key and value from notification message in a given subscription.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @return
     */
    private MultiValueMap<String, String> mapNotificationMessage(String aggregatedObject,
            JsonNode subscriptionJson) {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();
        ArrayNode arrNode = (ArrayNode) subscriptionJson.get("notificationMessageKeyValues");

        if (arrNode.isArray()) {
            LOGGER.debug("Running JMESPath extraction on form values.");

            for (final JsonNode objNode : arrNode) {
                String formKey = objNode.get("formkey").asText();
                String preJMESPathExtractionFormValue = objNode.get("formvalue").asText();

                JsonNode extractedJsonNode = jmespath.runRuleOnEvent(preJMESPathExtractionFormValue,
                        aggregatedObject);
                String postJMESPathExtractionFormValue = extractedJsonNode.toString()
                                                                          .replaceAll(
                                                                                  REGEX, "");

                LOGGER.debug("formValue after running the extraction: [{}] for formKey: [{}]",
                        postJMESPathExtractionFormValue, formKey);

                mapNotificationMessage.add(formKey, postJMESPathExtractionFormValue);
            }
        }
        return mapNotificationMessage;
    }
}