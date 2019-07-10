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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.utils.NotificationMeta;
import com.ericsson.ei.utils.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class HttpRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    private static final String JENKINS_CRUMB_ENDPOINT = "/crumbIssuer/api/json";

    private static final String AUTHENTICATION_TYPE_NO_AUTH = "NO_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH = "BASIC_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF = "BASIC_AUTH_JENKINS_CSRF";

    private SubscriptionField subscriptionField;

    private HttpRequestSender httpRequestSender;

    private NotificationMeta notificationMeta2;

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

    private HttpEntity<?> request;
    private String contentType;
    private HttpHeaders headers;
    private String username;
    private String password;

    HttpRequest (HttpRequestSender httpRequestSender, NotificationMeta notificationMeta) {
        this.httpRequestSender = httpRequestSender;
        this.notificationMeta2 = notificationMeta;
    }
    /**
     * Perform a HTTP request to a specific url. Returns the response.
     *
     * @throws AuthenticationException
     *
     */
    public boolean perform() throws AuthenticationException {
        boolean response = httpRequestSender.postDataMultiValue(this.url,
                this.mapNotificationMessage, this.headers);
        // TODO: send request
        return response;
    }

    public HttpRequest build() throws AuthenticationException {
        subscriptionField = new SubscriptionField(this.subscriptionJson);
        prepareHeaders();
        createRequest();

        return this;
    }

    private void createRequest() {
        boolean isApplicationXWwwFormUrlEncoded = contentType.equals(
                MediaType.APPLICATION_FORM_URLENCODED);
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
     * Prepares headers to be used when making a request with the method POST.
     *
     * @param url A String containing a URL
     * @param subscriptionJson Used to extract the rest post body media type from
     * @return headers
     * @throws AuthenticationException
     */
    private void prepareHeaders() throws AuthenticationException {
        this.headers = new HttpHeaders();
        setContentTypeInHeader();
        addAuthenticationData();
    }

    /**
     * Adds content type to the headers
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

        boolean authenticationDetailsProvided = isAuthenticationDetailsProvided(authType, username,
                password);
        if (!authenticationDetailsProvided) {
            return;
        }

        String encoding = Base64.getEncoder()
                                .encodeToString((username + ":" + password).getBytes());
        this.headers.add("Authorization", "Basic " + encoding);
        LOGGER.debug("Successfully added header for 'Authorization'");

        if (authType.equals(AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF)) {
            JsonNode crumb = fetchJenkinsCrumb(encoding);
            addJenkinsCrumbData(crumb);
        }

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

    /**
     * Tries to fetch a Jenkins crumb. Will return Jenkins crumb data in JSON format, or null if no
     * crumb was found.
     *
     * @param encoding
     * @return JenkinsJsonCrumbData
     * @throws AuthenticationException
     */
    private JsonNode fetchJenkinsCrumb(String encoding)
            throws AuthenticationException {
        try {
            URL url = buildJenkinsCrumbUrl(this.url);

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
        String baseUrl = notificationMeta2.extractBaseUrl(notificationMeta);
        URL url = new URL(baseUrl + JENKINS_CRUMB_ENDPOINT);
        return url;
    }

}
