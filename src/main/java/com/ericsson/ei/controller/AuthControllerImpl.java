package com.ericsson.ei.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @Value("${ldap.enabled:false}")
    private boolean ldapEnabled;

    @Override
    @CrossOrigin
    @ApiOperation(value = "To check is security enabled", response = String.class)
    public ResponseEntity<?> getAuth() {
        try {
            return new ResponseEntity<>("{\"security\":\"" + ldapEnabled + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To get login of current user", response = String.class)
    public ResponseEntity<?> getLogin() {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            return new ResponseEntity<>("{\"user\":\"" + currentUser + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "To check backend status", response = String.class)
    public ResponseEntity<?> getCheckStatus() {
        try {
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
