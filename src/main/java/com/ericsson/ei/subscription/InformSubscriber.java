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

import com.ericsson.ei.handlers.DateUtils;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import lombok.Getter;

/**
 * This class represents the REST POST notification mechanism and the alternate way to save the
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
     * This method extracts the mode of notification through which the subscriber
     * should be notified, from the subscription Object. And if the notification
     * fails, then it saved in the database.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson) {
        String subscriptionName = getSubscriptionField("subscriptionName", subscriptionJson);
        String notificationType = getSubscriptionField("notificationType", subscriptionJson);
        String notificationMeta = getSubscriptionField("notificationMeta", subscriptionJson);

        MultiValueMap<String, String> mapNotificationMessage = mapNotificationMessage(aggregatedObject,
                subscriptionJson);

        if (notificationType.trim().equals("REST_POST")) {
            LOGGER.debug("Notification through REST_POST");
            // Prepare notification meta
            notificationMeta = replaceParamsValuesWithAggregatedData(aggregatedObject, notificationMeta);

            // Prepare request headers
            HttpHeaders headers = prepareHeaders(notificationMeta, subscriptionJson);

            // Make HTTP request(s)
            boolean success = makeHTTPRequests(notificationMeta, mapNotificationMessage, headers);

            if (!success) {
                String missedNotification = prepareMissedNotification(aggregatedObject, subscriptionName, notificationMeta);
                LOGGER.debug("Prepared 'missed notification' document : " + missedNotification);
                mongoDBHandler.createTTLIndex(missedNotificationDataBaseName, missedNotificationCollectionName, "Time",
                        ttlValue);
                saveMissedNotificationToDB(missedNotification);
            }
        }

        if (notificationType.trim().equals("MAIL")) {
            LOGGER.debug("Notification through EMAIL");
            String subject = getSubscriptionField("emailSubject", subscriptionJson);
            emailSender.sendEmail(notificationMeta, String.valueOf((mapNotificationMessage.get("")).get(0)), subject);
        }
    }

    /**
     * This method attempts to make HTTP POST requests. If the request fails, it
     * is retried until the maximum number of failAttempts have been reached.
     *
     * @param notificationMeta
     *     The URL to send the request to
     * @param  mapNotificationMessage
     *     The body of the HTTP request
     * @param headers
     * @return success
     *     A boolean value depending on the outcome of the final HTTP request
     * */
    private boolean makeHTTPRequests(String notificationMeta, MultiValueMap<String, String> mapNotificationMessage,
                                     HttpHeaders headers) {
        boolean success = false;
        int requestTries = 0;

        do {
            requestTries++;
            success = httpRequestSender.postDataMultiValue(notificationMeta, mapNotificationMessage, headers);
            LOGGER.debug("After trying for " + requestTries + " time(s), the result is : " + success);
        } while (!success && requestTries <= failAttempt);

        return success;
    }

    /**
     * This method prepares headers to be used when making a rest call with the
     * method POST.
     *
     * @param notificationMeta
     *    A String containing a URL
     * @param subscriptionJson
     *     Used to extract the rest post body media type from
     * @return headers
     */
    private HttpHeaders prepareHeaders(String notificationMeta, JsonNode subscriptionJson) {
        HttpHeaders headers = new HttpHeaders();
        String headerContentMediaType = getSubscriptionField("restPostBodyMediaType", subscriptionJson);
        headers.setContentType(MediaType.valueOf(headerContentMediaType));
        LOGGER.debug("Successfully added header: 'restPostBodyMediaType':'{}'", headerContentMediaType);
        headers = addAuthenticationData(headers, notificationMeta, subscriptionJson);

        return headers;
    }

    /**
     * This method adds the authentication details to the headers.
     *
     * @param headers
     * @param notificationMeta
     *     A String containing a URL
     * @param subscriptionJson
     *     Used to extract the authentication type from
     * @return headers
     */
    private HttpHeaders addAuthenticationData(HttpHeaders headers, String notificationMeta, JsonNode subscriptionJson) {
        String authType = getSubscriptionField("authenticationType", subscriptionJson);
        if (!authType.equals("BASIC_AUTH")) {
            return headers;
        }

        String username = getSubscriptionField("userName", subscriptionJson);
        String password = getSubscriptionField("password", subscriptionJson);

        if (!username.equals("") && !password.equals("")) {
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headers.add("Authorization", "Basic " + encoding);
            LOGGER.debug("Successfully added header for 'Authorization'");

            // Adding jenkins crumb if any
            headers = addJenkinsCrumbData(headers, encoding, notificationMeta);

        } else {
            LOGGER.error(
                    "userName/password field in subscription is missing. Make sure both are provided for BASIC_AUTH.");
        }
        return headers;
    }

    /**
     * This method adds crumb to the headers if applicable.
     *
     * @param headers
     * @param encoding
     * @param notificationMeta
     *     A String containing a URL
     * @return headers
     *     Headers containing Jenkins crumb data
     */
    private HttpHeaders addJenkinsCrumbData(HttpHeaders headers, String encoding, String notificationMeta) {
        JsonNode jenkinsJsonCrumbData = fetchJenkinsCrumbIfAny(encoding, notificationMeta);
        if (jenkinsJsonCrumbData != null) {
            String crumbKey = jenkinsJsonCrumbData.get("crumbRequestField").asText();
            String crumbValue = jenkinsJsonCrumbData.get("crumb").asText();
            headers.add(crumbKey, crumbValue);
            LOGGER.debug("Successfully added header: " + String.format("'%s':'%s'", crumbKey, crumbValue));
        }
        return headers;
    }

    /**
     * Tries to fetch a Jenkins crumb. Will return Jenkins crumb data in JSON
     * format, or null if no crumb was found.
     *
     * @param encoding
     * @param notificationMeta
     *     A String containing a URL
     * @return JenkinsJsonCrumbData
     */
    private JsonNode fetchJenkinsCrumbIfAny(String encoding, String notificationMeta) {
        URL url;
        try {
            String baseUrl = extractBaseUrl(notificationMeta);
            url = new URL(baseUrl + JENKINS_CRUMB_ENDPOINT);
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to format url to collect jenkins crumb");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encoding);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> response = httpRequestSender.makeGetRequest(url.toString(), headers);

        if (response == null || response.getStatusCodeValue() != HttpStatus.OK.value()) {
            LOGGER.debug("No jenkins crumb found, most likely not jenkins or jenkins with crumb disabled");
            return null;
        }

        LOGGER.debug("Successfully fetched Jenkins crumb.");
        JsonNode JenkinsJsonCrumbData = response.getBody();
        return JenkinsJsonCrumbData;
    }

    /**
     * This method extracts the url parameters from the notification meta. It runs the parameter values
     * through JMESPath to replace wanted parameter values with data from the aggregated object. It then
     * reformats the notification meta containing the new parameters.
     *
     * @param aggregatedObject
     *     The aggregated object contains the url parameters which will be updated
     * @param notificationMeta
     *     A String containing a URL
     * @return String
     */
    private String replaceParamsValuesWithAggregatedData(String aggregatedObject, String notificationMeta) {
        if (!notificationMeta.contains("?")) {
            return notificationMeta;
        }
        LOGGER.debug("Unformatted notificationMeta = " + notificationMeta);

        try {
            String baseUrl = extractBaseUrl(notificationMeta);
            String contextPath = extractContextPath(notificationMeta);
            List<NameValuePair> params = extractUrlParameters(notificationMeta);
            LOGGER.debug("Notification meta in parts:\n ## Base Url: {}\n ## Context Path: {}\n ## URL Parameters: {} ",
                    baseUrl, contextPath, params);

            List<NameValuePair> processedParams = processJMESPathParameters(aggregatedObject, params);
            LOGGER.debug("JMESPath processed parameters :\n ## {}", processedParams);
            String encodedQuery = URLEncodedUtils.format(processedParams, "UTF8");

            notificationMeta = String.format("%s%s?%s", baseUrl, contextPath, encodedQuery);
            LOGGER.debug("Formatted notificationMeta = " + notificationMeta);

            return notificationMeta;
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to extract parameters: " + e.getMessage());
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
    private List<NameValuePair> extractUrlParameters(String notificationMeta) throws MalformedURLException {
        URL url = new URL(notificationMeta);
        String query = url.getQuery();
        List<NameValuePair> params = splitQuery(query);
        return params;
    }

    /**
     * Splits a query string into one pair for each key and value. Loops said pairs
     * and extracts the key and value as KeyValuePair. Adds KeyValuePair to list.
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
     * Runs JMESPath rules on values in a list of KeyValuePair and replaces the
     * value with extracted data.
     *
     * @param aggregatedObject
     * @param params
     * @return List<NameValuePair>
     * @throws UnsupportedEncodingException
     */
    private List<NameValuePair> processJMESPathParameters(String aggregatedObject, List<NameValuePair> params)
            throws UnsupportedEncodingException {
        List<NameValuePair> processedParams = new ArrayList<>();

        for (NameValuePair param : params) {
            String name = URLDecoder.decode(param.getName(), "UTF-8");
            String value = URLDecoder.decode(param.getValue(), "UTF-8");

            LOGGER.debug("Input parameter key and value: " + name + " : " + value);
            value = jmespath.runRuleOnEvent(value.replaceAll(REGEX, ""), aggregatedObject).toString().replaceAll(REGEX,
                    "");

            LOGGER.debug("Formatted parameter key and value: " + name + " : " + value);
            processedParams.add(new BasicNameValuePair(name, value));
        }
        return processedParams;
    }

    /**
     * Returns the base url from the notification meta.
     *
     * @param notificationMeta
     *     A String containing a URL
     * @return
     *     The base url, which excludes context path and parameters.
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
     * This method returns the context path from the notification meta.
     *
     * @param notificationMeta
     *     A String containing a URL
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
     * This method saves the missed Notification into a single document in
     * the database.
     */
    private void saveMissedNotificationToDB(String missedNotification) {
        try {
            mongoDBHandler.insertDocument(missedNotificationDataBaseName, missedNotificationCollectionName,
                    missedNotification);
            LOGGER.debug("Notification saved in the database");
        } catch (MongoWriteException e) {
            LOGGER.debug("Failed to insert the notification into database");
        }
    }

    /**
     * This method prepares the document to be saved in the missed notification database.
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
            LOGGER.error(e.getMessage(), e);
        }
        document.put("AggregatedObject", BasicDBObject.parse(aggregatedObject));
        return document.toString();
    }

    /**
     * This method, given the field name, returns its value.
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
     * This method extracts key and value from notification message
     * in a given subscription.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @return
     */
    private MultiValueMap<String, String> mapNotificationMessage(String aggregatedObject, JsonNode subscriptionJson) {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();
        ArrayNode arrNode = (ArrayNode) subscriptionJson.get("notificationMessageKeyValues");

        if (arrNode.isArray()) {
            LOGGER.debug("Running JMESPath extraction on form values.");

            for (final JsonNode objNode : arrNode) {
                String formKey = objNode.get("formkey").asText();
                String preJMESPathExtractionFormValue = objNode.get("formvalue").asText();

                JsonNode extractedJsonNode = jmespath.runRuleOnEvent(preJMESPathExtractionFormValue, aggregatedObject);
                String postJMESPathExtractionFormValue = extractedJsonNode.toString().replaceAll(REGEX, "");

                LOGGER.debug("formValue after running the extraction: [{}] for formKey: [{}]",
                        postJMESPathExtractionFormValue, formKey);

                mapNotificationMessage.add(formKey, postJMESPathExtractionFormValue);
            }
        }
        return mapNotificationMessage;
    }
}