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
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.io.InputStream;

@Component
@CrossOrigin
@Api(value = "Get Templates", description = "REST endpoints for getting templates")
public class DownloadControllerImpl implements DownloadController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadControllerImpl.class);

    @Override
    public ResponseEntity<?> getDownload() {
        try {
            JSONObject response = new JSONObject();
            response.put("subscriptions", "/download/subscriptionsTemplate");
            response.put("rules", "/download/rulesTemplate");
            response.put("events", "/download/eventsTemplate");
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to get information about download endpoints. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getSubscriptionsTemplate() {
        try {
            InputStream is = getClass().getResourceAsStream("/templates/subscriptionsTemplate.json");
            return new ResponseEntity<>(IOUtils.toByteArray(is), HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("Subscriptions template file is not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            String errorMessage = "Failed to download subscriptions template file. Error message:\n" + e.getMessage();
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRulesTemplate() {
        try {
            InputStream is = getClass().getResourceAsStream("/templates/rulesTemplate.json");
            return new ResponseEntity<>(IOUtils.toByteArray(is), HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("Rules template file is not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            String errorMessage = "Failed to download rules template file. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getEventsTemplate() {
        try {
            InputStream is = getClass().getResourceAsStream("/templates/eventsTemplate.json");
            return new ResponseEntity<>(IOUtils.toByteArray(is), HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>("Events template file is not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            String errorMessage = "Failed to download events template file. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
