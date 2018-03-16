package com.ericsson.ei.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.io.InputStream;

@Component
@CrossOrigin
@Api(value = "Get template", description = "REST end-points for template subscription")
public class SubscriptionTemplateControllerImpl implements SubscriptiontemplateController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionTemplateControllerImpl.class);

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public ResponseEntity<?> getDownloadSubscriptiontemplate() {
        try {
            InputStream is = getClass().getResourceAsStream("/subscriptionsTemplate.json");
            return new ResponseEntity<>(IOUtils.toByteArray(is), new HttpHeaders(), HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error " + e.getMessage());
            return new ResponseEntity<>("File not found", HttpStatus.OK);
        }
    }
}
