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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Endpoints /auth/login and /auth/checkStatus should be secured in case LDAP is enabled
 * Endpoint /auth should be not secured
 */
@Component
@CrossOrigin
@Api(value = "Auth", description = "REST endpoints for authentication and authorization")
public class AuthControllerImpl implements AuthController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AuthControllerImpl.class);

    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;

    @Override
    @CrossOrigin
    @ApiOperation(value = "To check if security is enabled", response = String.class)
    public ResponseEntity<?> getAuth() {
        try {
            return new ResponseEntity<>(new JSONObject().put("security", ldapEnabled).toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to check if security is enabled. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To get login of current user", response = String.class)
    public ResponseEntity<?> getLogin() {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            return new ResponseEntity<>(new JSONObject().put("user", currentUser).toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to log in user. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To check backend status", response = String.class)
    public ResponseEntity<?> getCheckStatus() {
        try {
            return new ResponseEntity<>("Backend server is up and running", HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to check backend status. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
