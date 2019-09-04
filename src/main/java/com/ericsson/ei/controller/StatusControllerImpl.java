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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.ericsson.ei.handlers.StatusHandler;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;

/**
 * Endpoint /status should display EI back-end status and services Eiffel Intelligence is dependent
 * on.
 */
@Component
@CrossOrigin
public class StatusControllerImpl implements StatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusControllerImpl.class);
    private static final String ERROR_MESSAGE = "Failed to check back-end status.";

    @Autowired
    private StatusHandler statusHandler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @CrossOrigin
    @ApiOperation(value = "To check back-end status", response = String.class)
    public ResponseEntity<?> getStatus() {
        try {
            final JsonNode status = statusHandler.getCurrentStatus();
            final String statusString = objectMapper.writerWithDefaultPrettyPrinter()
                                                    .writeValueAsString(status);
            return new ResponseEntity<>(statusString, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(ERROR_MESSAGE, e);
            final String errorJsonAsString = ResponseMessage.createJsonMessage(ERROR_MESSAGE);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
