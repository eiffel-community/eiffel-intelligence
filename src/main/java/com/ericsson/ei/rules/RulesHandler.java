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
package com.ericsson.ei.rules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RulesHandler {

    @Value("${rules.path}")
    private String rulesFilePath;

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();
    private JsonNode parsedJson;

    public RulesHandler() throws Exception {
        if (rulesFilePath == null) {
            rulesFilePath = System.getProperty("rules.path");
        }
    }

    @PostConstruct
    public void init() throws Exception {
        if (parsedJson == null) {
            String jsonFileContent = readRulesFileContent();
            setParsedJson(jsonFileContent);
        }
    }

    /**
     * Parses a given String into a JsonNode.
     *
     * @param jsonContent
     *            the content to be parsed
     * @throws JsonProcessingException
     * @throws IOException
     */
    public void setParsedJson(String jsonContent) throws JsonProcessingException, IOException {
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJson = objectmapper.readTree(jsonContent);
    }

    /**
     * Gets applicable rule for a given event.
     *
     * @param event
     *            the event string
     * @return rules object
     */
    public RulesObject getRulesForEvent(String event) {
        Iterator<JsonNode> iter = parsedJson.iterator();
        while (iter.hasNext()) {
            JsonNode rule = iter.next();
            String typeRule = rule.get("TypeRule").toString();

            // Remove the surrounding double quote signs
            typeRule = typeRule.replaceAll("^\"|\"$", "");

            JsonNode result = jmesPathInterface.runRuleOnEvent(typeRule, event);
            JsonNode type = rule.get("Type");

            if (result.equals(type)) {
                return new RulesObject(rule);
            }
        }
        return null;
    }

    /**
     * Reads the content of a given rules file path or URL.
     *
     * @return the rules file content
     * @throws Exception
     */
    private String readRulesFileContent() throws Exception {
        String content;
        if (isPathUsingScheme(rulesFilePath)) {
            content = readRulesFileFromURI();
        } else {
            content = readRulesFileFromPath();
        }
        if (content.isEmpty()) {
            throw new Exception("Rules content cannot be empty");
        }
        return content;
    }

    /**
     * Reads the rules file content from a URI.
     *
     * @return rules file content
     * @throws IOException
     * @throws URISyntaxException
     */
    private String readRulesFileFromURI() throws IOException, URISyntaxException {
        return IOUtils.toString(new URI(rulesFilePath), "UTF-8");
    }

    /**
     * Reads the rules file content from a full or relative path.
     *
     * @return rules file content
     * @throws IOException
     * @throws Exception
     */
    private String readRulesFileFromPath() throws IOException {
        String rulesJsonFileContent = null;
        try (InputStream inputStream = this.getClass().getResourceAsStream(rulesFilePath)) {
            if (inputStream == null) {
                rulesJsonFileContent = FileUtils.readFileToString(new File(rulesFilePath), Charset.defaultCharset());
            } else {
                rulesJsonFileContent = IOUtils.toString(inputStream, "UTF-8");
            }
        }
        return rulesJsonFileContent;
    }

    /**
     * Checks if the path is using a scheme.
     *
     * @param path
     *            the rules file path
     * @return true if there is a match, false otherwise
     */
    private boolean isPathUsingScheme(String path) {
        String schemeRegex = "https?|file";
        try {
            URI uri = new URI(path);
            return uri.getScheme().matches(schemeRegex);
        } catch (Exception e) {
            return false;
        }
    }
}