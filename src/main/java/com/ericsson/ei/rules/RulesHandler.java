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

import java.io.*;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;

@Component
public class RulesHandler {
    private static Logger log = LoggerFactory.getLogger(RulesHandler.class);

    @Value("${rules.path}") private String jsonFilePath;

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();
    private static String jsonFileContent;
    private static JsonNode parsedJason;

    @PostConstruct public void init() {
        if (parsedJason == null) {
            try {
                InputStream in = this.getClass().getResourceAsStream(jsonFilePath);
                if(in == null) {
                    jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath));
                } else {
                    jsonFileContent = getContent(in);
                }
                ObjectMapper objectmapper = new ObjectMapper();
                parsedJason = objectmapper.readTree(jsonFileContent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void setRulePath(String path) {
        this.jsonFilePath = path;
        try {
            this.jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            parsedJason = objectmapper.readTree(jsonFileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RulesObject getRulesForEvent(String event) {
        String typeRule;
        JsonNode type;
        JsonNode result;
        Iterator<JsonNode> iter = parsedJason.iterator();
        while(iter.hasNext()) {
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

    private String getContent(InputStream inputStream){
        try {


            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");}
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}