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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RulesHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesHandler.class);

    private static final String EI_HOME_DEFAULT_NAME = ".eiffel-intelligence";

    @Value("${rules.path}")
    private String rulesFilePath;

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();
    private static String jsonFileContent;
    private static JsonNode parsedJson;

    public RulesHandler() {
        super();
    }

    @PostConstruct
    public void init() {
        if (parsedJson == null) {
            try {
                jsonFileContent = readRulesFileContent(rulesFilePath);
                ObjectMapper objectmapper = new ObjectMapper();
                parsedJson = objectmapper.readTree(jsonFileContent);
            } catch (Exception e) {
                LOGGER.error("RulesHandler Init: Failed to read Rules file: " + rulesFilePath, e.getMessage(), e);
            }
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
     * Reads the content of a given rules file path into a String
     *
     * @param path
     *            the source file path
     * @return the file content as a String
     * @throws Exception
     */
    public String readRulesFileContent(String path) throws Exception {
        setRulesFilePath(path);
        downloadRulesFile(path);
        return readRulesJsonFileContent();
    }

    /**
     * Sets the rules file path and loads the file.
     *
     * @param path
     *            the rules file path
     */
    public void setRulePath(String path) {
        this.rulesFilePath = path;
        try {
            RulesHandler.jsonFileContent = FileUtils.readFileToString(new File(rulesFilePath), Charset.defaultCharset());
            ObjectMapper objectmapper = new ObjectMapper();
            parsedJson = objectmapper.readTree(jsonFileContent);
        } catch (IOException e) {
            LOGGER.error("Failed to read Rules file: " + rulesFilePath, e.getMessage(), e);
        }
    }

    /**
     * Gets applicable rule for a given event.
     *
     * @param event
     *            the event string
     * @return rules object
     */
    public RulesObject getRulesForEvent(String event) {
        String typeRule;
        JsonNode type;
        JsonNode result;
        Iterator<JsonNode> iter = parsedJson.iterator();
        while (iter.hasNext()) {
            JsonNode rule = iter.next();
            typeRule = rule.get("TypeRule").toString();

            // Remove the surrounding double quote signs
            typeRule = typeRule.replaceAll("^\"|\"$", "");

            result = jmesPathInterface.runRuleOnEvent(typeRule, event);
            type = rule.get("Type");

            if (result.equals(type)) {
                return new RulesObject(rule);
            }
        }
        return null;
    }

    /**
     * Gets content from input stream.
     *
     * @param inputStream
     *            the input stream
     * @return string with content
     */
    private String getContent(InputStream inputStream) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Sets the path where the rules file is located.
     *
     * @param path
     *            the rules file path
     * @throws IOException
     */
    private void setRulesFilePath(String path) throws IOException {
        if (isPathURL(path)) {
            rulesFilePath = getRulesFilePath();
        } else {
            rulesFilePath = path;
        }
    }

    /**
     * If the path is a URL it will be downloaded and placed into the home folder.
     *
     * @param path
     *            the rules file path
     * @throws IOException
     */
    private void downloadRulesFile(String path) throws IOException {
        if (isPathURL(path)) {
            downloadRuleFileToHomeDirectory(path);
        }
    }

    /**
     * Downloads rules file from URL to rules file path.
     *
     * @param url
     *            the url of rules file
     * @throws IOException
     */
    private void downloadRuleFileToHomeDirectory(String url) throws IOException {
        URL source = new URL(url);
        final File destination = new File(rulesFilePath);
        FileUtils.copyURLToFile(source, destination);
    }

    /**
     * Reads the content of the rules file.
     *
     * @return rules file content
     * @throws Exception
     */
    private String readRulesJsonFileContent() throws Exception {
        String rulesJsonFileContent = null;
        InputStream inputStream = this.getClass().getResourceAsStream(rulesFilePath);
        if (inputStream == null) {
            rulesJsonFileContent = FileUtils.readFileToString(new File(rulesFilePath), Charset.defaultCharset());
        } else {
            rulesJsonFileContent = getContent(inputStream);
        }
        if (rulesJsonFileContent.equals("")) {
            throw new Exception("Rules content cannot be empty");
        }
        return rulesJsonFileContent;
    }

    /**
     * Checks if the given path is a URL.
     *
     * @param path
     *            the rules file path
     * @return
     */
    private boolean isPathURL(String path) {
        String protocolRegex = "https?";
        try {
            URI uri = new URI(path);
            return uri.getScheme().matches(protocolRegex);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Compiles the rules file home directory path.
     *
     * @return the rules file path
     */
    private String getRulesFilePath() {
        String homeFolder = System.getProperty("user.home");
        String rulesFileName = "rules.json";
        return Paths.get(homeFolder, EI_HOME_DEFAULT_NAME, rulesFileName).toString();
    }
}