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
package com.ericsson.ei.controller;

import com.ericsson.ei.controller.model.ParseInstanceInfoEI;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@CrossOrigin
@Api(value = "information", tags = {"Information"})
public class InformationControllerImpl implements InformationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InformationControllerImpl.class);

    @Autowired
    private ParseInstanceInfoEI instanceInfo;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Shows information about Eiffel Intelligence backend")
    public ResponseEntity<?> getInformation(HttpServletRequest httpRequest) {
        try {
            String info = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(instanceInfo);
            LOGGER.debug("EI backend information is parsed successfully");
            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to parse EI backend information.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
