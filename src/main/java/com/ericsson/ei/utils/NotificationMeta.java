package com.ericsson.ei.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;

@Component
public class NotificationMeta {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMeta.class);
    private static final String REGEX = "^\"|\"$";
    // Regular expression for replacing unexpected character like \"

    @Autowired
    private JmesPathInterface jmespath;

    public String runJmesPathOnParameters(String notificationMeta, String aggregatedObject) {
        String processedNotificationMeta = replaceParamsValuesWithAggregatedData(notificationMeta, aggregatedObject);
        return processedNotificationMeta;
    }


    /**
     * Extracts the url parameters from the notification meta. It runs the parameter values through
     * JMESPath to replace wanted parameter values with data from the aggregated object. It then
     * reformats the notification meta containing the new parameters.
     * @param notificationMeta
     *
     * @return String
     */
    private String replaceParamsValuesWithAggregatedData(String notificationMeta, String aggregatedObject) {
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
     * Returns the base url from the notification meta.
     *
     * @param notificationMeta A String containing a URL
     * @return The base url, which excludes context path and parameters.
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public String extractBaseUrl(String notificationMeta) throws MalformedURLException {
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

            LOGGER.debug("Input parameter key and value: {} : {}", name, value);
            value = jmespath.runRuleOnEvent(value.replaceAll(REGEX, ""), aggregatedObject)
                            .toString()
                            .replaceAll(REGEX, "");

            LOGGER.debug("Formatted parameter key and value: {} : {}", name, value);
            processedParams.add(new BasicNameValuePair(name, value));
        }
        return processedParams;
    }
}
