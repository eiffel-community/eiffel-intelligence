package com.ericsson.ei.subscription;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class HttpRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    private static final String JENKINS_CRUMB_ENDPOINT = "/crumbIssuer/api/json";
    // Regular expression for replacing unexpected character like \"
    private static final String REGEX = "^\"|\"$";

    private static final String AUTHENTICATION_TYPE_NO_AUTH = "NO_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH = "BASIC_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF = "BASIC_AUTH_JENKINS_CSRF";

    @Autowired
    private SubscriptionHandler subscriptionHandler;

    @Autowired
    private HttpRequestSender httpRequestSender;

    @Autowired
    private JmesPathInterface jmespath;

    @Setter
    @Getter
    @Accessors(chain = true)
    private String aggregatedObject;

    @Setter
    @Getter
    @Accessors(chain = true)
    private JsonNode subscriptionJson;

    @Setter
    @Getter
    @Accessors(chain = true)
    private String notificationMeta;

    private HttpHeaders headers;
    private String username;
    private String password;

    public void send() {
        // TODO Auto-generated method stub

    }

    public HttpRequest prepare() throws AuthenticationException {
        prepareHeaders();
        replaceParamsValuesWithAggregatedData();
        // TODO Auto-generated method stub
        return this;
    }

    /**
     * Prepares headers to be used when making a rest call with the method POST.
     *
     * @param notificationMeta A String containing a URL
     * @param subscriptionJson Used to extract the rest post body media type from
     * @return headers
     * @throws AuthenticationException
     */
    private HttpHeaders prepareHeaders()
            throws AuthenticationException {
        this.headers = new HttpHeaders();
        String headerContentMediaType = subscriptionHandler.getSubscriptionField("restPostBodyMediaType",
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
        String authType = subscriptionHandler.getSubscriptionField("authenticationType", subscriptionJson);
        String username = subscriptionHandler.getSubscriptionField("userName", subscriptionJson);
        String password = subscriptionHandler.getSubscriptionField("password", subscriptionJson);

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
            LOGGER.info("Successfully added header: " + String.format("'%s':'%s'", crumbKey,
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
     * @return String
     */
    private void replaceParamsValuesWithAggregatedData() {
        if (!notificationMeta.contains("?")) {
            return;
        }
        LOGGER.debug("Unformatted notificationMeta = " + this.notificationMeta);

        try {
            String baseUrl = extractBaseUrl(this.notificationMeta);
            String contextPath = extractContextPath(this.notificationMeta);
            List<NameValuePair> params = extractUrlParameters(this.notificationMeta);
            LOGGER.debug("Notification meta in parts:\n ## Base Url: "
                    + "{}\n ## Context Path: {}\n ## URL Parameters: {} ", baseUrl, contextPath,
                    params);

            List<NameValuePair> processedParams = processJMESPathParameters(aggregatedObject,
                    params);
            LOGGER.debug("JMESPath processed parameters :\n ## {}", processedParams);
            String encodedQuery = URLEncodedUtils.format(processedParams, "UTF8");

            this.notificationMeta = String.format("%s%s?%s", baseUrl, contextPath, encodedQuery);
            LOGGER.debug("Formatted notificationMeta = " + this.notificationMeta);

        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to extract parameters: " + e.getMessage());
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

            LOGGER.debug("Input parameter key and value: " + name + " : " + value);
            value = jmespath.runRuleOnEvent(value.replaceAll(REGEX, ""), aggregatedObject)
                            .toString()
                            .replaceAll(REGEX, "");

            LOGGER.debug("Formatted parameter key and value: " + name + " : " + value);
            processedParams.add(new BasicNameValuePair(name, value));
        }
        return processedParams;
    }
}
