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
    private String jsonFilePath;

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();
    private static String jsonFileContent;
    private static JsonNode parsedJson;
    private String eiHomePath;

    public RulesHandler() {
        super();
    }

    public void setParsedJson(String jsonContent) throws JsonProcessingException, IOException {
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJson = objectmapper.readTree(jsonContent);
    }

    public String readRuleFileContent(String path) throws Exception {
        String ruleJsonFileContent = null;
        String ruleFilePath = "";
        if (checkIfPathIsURLAndJSON(path)) {
            setEIHomePath();
            ruleFilePath = downloadRuleFileToHomeDirectory(path);
        } else {
            ruleFilePath = path;
        }
        if (!ruleFilePath.equals("")) {
            ruleJsonFileContent = readRulesFileContent(ruleFilePath);
        }
        if (ruleFilePath.equals("") || ruleJsonFileContent.equals("")) {
            throw new Exception("Rules content cannot be empty");
        }
        return ruleJsonFileContent;
    }

    public String readRulesFileContent(String ruleFilePath) throws IOException {
        String ruleJsonFileContent = null;
        InputStream in = this.getClass().getResourceAsStream(ruleFilePath);
        if (in == null) {
            ruleJsonFileContent = FileUtils.readFileToString(new File(ruleFilePath), Charset.defaultCharset());
        } else {
            ruleJsonFileContent = getContent(in);
        }
        return ruleJsonFileContent;
    }

    public boolean checkIfPathIsURLAndJSON(String path) {
        String urlAndJsonRegex = "https?:\\/\\/.*\\.json";
        return path.matches(urlAndJsonRegex);
    }

    public void setEIHomePath() {
        String homeFolder = System.getProperty("user.home");
        eiHomePath = Paths.get(homeFolder, EI_HOME_DEFAULT_NAME).toString();
    }

    public String downloadRuleFileToHomeDirectory(String url) {
        String downloadPath = "";
        try {
            downloadPath = downloadURLToFile(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to create URL object.\nURL: {}\nError: {}", url, e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Failed to download file.\nURL: {}\nError: {}", url, e.getMessage());
        }
        return downloadPath;
    }

    public String downloadURLToFile(String url) throws IOException {
        URL source = new URL(url);
        String fileName = source.getFile();
        String downloadPath = eiHomePath + fileName;
        final File destination = new File(downloadPath);
        FileUtils.copyURLToFile(source, destination);
        return downloadPath;
    }

    @PostConstruct
    public void init() {
        if (parsedJson == null) {
            try {
                jsonFileContent = readRuleFileContent(jsonFilePath);
                ObjectMapper objectmapper = new ObjectMapper();
                parsedJson = objectmapper.readTree(jsonFileContent);
            } catch (Exception e) {
                LOGGER.error("RulesHandler Init: Failed to read Rules file: " + jsonFilePath, e.getMessage(), e);
            }
        }
    }

    public void setRulePath(String path) {
        this.jsonFilePath = path;
        try {
            RulesHandler.jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath), Charset.defaultCharset());
            ObjectMapper objectmapper = new ObjectMapper();
            parsedJson = objectmapper.readTree(jsonFileContent);
        } catch (IOException e) {
            LOGGER.error("Failed to read Rules file: " + jsonFilePath, e.getMessage(), e);
        }
    }

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

}