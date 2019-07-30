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
public class UrlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlParser.class);
    private static final String REGEX = "^\"|\"$";
    // Regular expression for replacing unexpected character like \"

    @Autowired
    private JmesPathInterface jmespath;

    /**
     * Extracts the url parameters from the given url. It runs the parameter values through JMESPath
     * to replace wanted parameter values with data from the aggregated object. It then reformats
     * the url containing the new parameters.
     *
     * @param url
     *
     * @return String
     */
    public String runJmesPathOnParameters(String url, String aggregatedObject) {
        if (!url.contains("?")) {
            return url;
        }
        LOGGER.debug("Unformatted notificationMeta = " + url);

        try {
            String baseUrl = extractBaseUrl(url);
            String contextPath = extractContextPath(url);
            List<NameValuePair> params = extractUrlParameters(url);
            LOGGER.debug("Url in parts:\n ## Base Url: "
                    + "{}\n ## Context Path: {}\n ## URL Parameters: {} ", baseUrl, contextPath,
                    params);

            List<NameValuePair> processedParams = processJMESPathParameters(aggregatedObject,
                    params);
            LOGGER.debug("JMESPath processed parameters :\n ## {}", processedParams);
            String encodedParams = URLEncodedUtils.format(processedParams, "UTF8");

            url = String.format("%s%s?%s", baseUrl, contextPath, encodedParams);
            LOGGER.debug("Formatted url = {}", url);

            return url;
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to extract parameters.", e);
            return url;
        }
    }

    /**
     * Returns the base url from the given String.
     *
     * @param url A String containing a URL
     * @return The base url, which excludes context path and parameters.
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public String extractBaseUrl(String url) throws MalformedURLException {
        URL absoluteUrl = new URL(url);
        String protocol = absoluteUrl.getProtocol();
        String authority = absoluteUrl.getAuthority();
        return String.format("%s://%s", protocol, authority);
    }

    /**
     * Returns the context path from the url.
     *
     * @param url A String containing a URL
     * @return contextPath
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String extractContextPath(String url) throws MalformedURLException {
        URL absoluteUrl = new URL(url);
        String contextPath = absoluteUrl.getPath();
        return contextPath;
    }

    /**
     * Extracts the query from the url and returns them as a list of KeyValuePair.
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    private List<NameValuePair> extractUrlParameters(String url)
            throws MalformedURLException {
        URL absoluteUrl = new URL(url);
        String query = absoluteUrl.getQuery();
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
}
