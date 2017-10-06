package com.ericsson.ei.rules;

import java.io.*;
import java.util.Iterator;
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
                jsonFileContent = getContent(in);
                ObjectMapper objectmapper = new ObjectMapper();
                parsedJason = objectmapper.readTree(jsonFileContent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void setRulePath(String path) {
        this.jsonFilePath = path;
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