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
package com.ericsson.ei.jmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.jackson.JacksonRuntime;

/**
 * Expose JMESPath functionality
 * 
 */
@Component
public class JmesPathInterface {

    static Logger log = (Logger) LoggerFactory.getLogger(JmesPathInterface.class);

    private JmesPath<JsonNode> jmespath;

    public JmesPathInterface() {
        FunctionRegistry defaultFunctions = FunctionRegistry.defaultRegistry();
        FunctionRegistry customFunctions = defaultFunctions.extend(new DiffFunction());
        customFunctions = customFunctions.extend(new IncompletePathContainsFunction());
        customFunctions = customFunctions.extend(new IncompletePathFilterFunction());
        jmespath = new JacksonRuntime(customFunctions);
    }

    public JsonNode runRuleOnEvent(String rule, String input) {
        JsonNode event = null;
        if (input == null) {
            input = "";
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode result = objectMapper.createObjectNode();
        try {
            Expression<JsonNode> expression = jmespath.compile(rule);
            event = objectMapper.readValue(input, JsonNode.class);
            result = expression.search(event);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

        return result;
    }
}
