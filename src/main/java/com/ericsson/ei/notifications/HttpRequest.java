package com.ericsson.ei.notifications;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.utils.SpringContext;
import com.ericsson.ei.utils.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class HttpRequest {

    static class HttpRequestFactory {
        // Used to enable mocking of this class in tests.
        HttpRequest createHttpRequest() {
            return new HttpRequest();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

    private static final String AUTHENTICATION_TYPE_NO_AUTH = "NO_AUTH";
    private static final String AUTHENTICATION_TYPE_BASIC_AUTH_JENKINS_CSRF = "BASIC_AUTH_JENKINS_CSRF";

    private SubscriptionField subscriptionField;

    // Manual wired
    private HttpRequestSender httpRequestSender = SpringContext.getBean(HttpRequestSender.class);
    private JenkinsCrumb jenkinsCrumb = SpringContext.getBean(JenkinsCrumb.class);
    private UrlParser urlParser = SpringContext.getBean(UrlParser.class);

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
     * @return response     A boolean value of the request response
     * @throws AuthenticationException
     */
    public boolean perform() throws AuthenticationException {
        boolean response = httpRequestSender.postDataMultiValue(this.url, this.request);
        return response;
    }

    /**
     * Builds a HTTP request with headers.
     * */
    public HttpRequest build() throws AuthenticationException {
        this.subscriptionField = new SubscriptionField(this.subscriptionJson);
        prepareHeaders();
        createRequest();
        this.url = urlParser.runJmesPathOnParameters(this.url, this.aggregatedObject);

        return this;
    }

    /**
     * Prepares headers to be used in a POST request.
     * POST.
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
     * */
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
            JsonNode crumb = jenkinsCrumb.fetchJenkinsCrumb(encoding, this.url);
            addJenkinsCrumbData(crumb);
        }

    }

    /**
     * Returns a boolean indicating that authentication details was provided in
     * the subscription.
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

}
