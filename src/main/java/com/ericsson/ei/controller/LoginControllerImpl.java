package com.ericsson.ei.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@CrossOrigin
@Api(value = "Auth", description = "REST endpoints for authentication and authorization")
public class LoginControllerImpl implements LoginController {

    @Override
    @CrossOrigin
    @ApiOperation(value = "To login user", response = String.class)
    public ResponseEntity<?> getAuthLogin() {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            return new ResponseEntity<>("{\"user\":\"" + currentUser + "\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
