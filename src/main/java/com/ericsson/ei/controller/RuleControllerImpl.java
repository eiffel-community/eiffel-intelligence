/*
   Copyright 2018 Ericsson AB.
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
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@CrossOrigin
@Api(value = "Rules", tags = {"Rules"})
public class RuleControllerImpl implements RuleController{

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleControllerImpl.class);

    @Autowired
    private RulesHandler rulesHandler;

    @Value("${rules.path}")
    private String rulesPath;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Get the active rules from Eiffel Intelligence", response = String.class)
    public ResponseEntity<?> getRules(final HttpServletRequest httpRequest) {
        JsonNode rulesContent = rulesHandler.getRulesContent();
        ObjectMapper objectmapper = new ObjectMapper();
        try {
            String contentAsString = objectmapper.writeValueAsString(rulesContent);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("path", rulesPath);
            jsonResult.put("content", contentAsString);
            String result = jsonResult.toString();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            String errorMessage = "Internal Server Error: Failed to parse the rules content.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
