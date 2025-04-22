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

import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.queryservice.ProcessFailedNotification;
import com.ericsson.ei.utils.ResponseMessage;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;

/**
 * This class contains logic for retrieving failed notifications for the given subscription.
 */
@Component
@CrossOrigin
@Api(tags = {"Failed notifications"}, description = "Fetch failed notifications of a subscription")
public class FailedNotificationControllerImpl implements FailedNotificationController {

    private final static Logger LOGGER = LoggerFactory.getLogger(
            FailedNotificationControllerImpl.class);

    @Autowired
    private ProcessFailedNotification processFailedNotification;

    /**
     * This method is responsible for the REST GET mechanism to extract the data on the basis of the
     * subscription name from the Failed Notification Object.
     *
     * @param subscriptionNames
     */
    @Override
    @ApiOperation(value = "Retrieve failed notifications", tags = { "Failed notifications" },
            response = QueryResponse.class)
    public ResponseEntity<?> getFailedNotifications(
            @RequestParam(value = "subscriptionNames", required = true) final String subscriptionNames,
            final HttpServletRequest httpRequest) {
        try {
            String[] subscriptionArray = subscriptionNames.replaceAll("\\s+", "").split(",");
            JSONArray notifications = fetchFailedNotifications(subscriptionArray);
            ResponseEntity<?> response = createResponse(notifications);
            return response;
        } catch (Exception e) {
            String errorMessage = "Failed to fetch failed notifications for subscription(s): " + subscriptionNames;
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> createResponse(JSONArray notifications) {
        HttpStatus httpStatus = HttpStatus.OK;
        String jsonResponse = notifications.toString();
        return new ResponseEntity<>(jsonResponse, httpStatus);
    }

    private JSONArray fetchFailedNotifications(String[] subscriptionArray) {
        JSONArray notifications = new JSONArray();
        for (String name : subscriptionArray) {
            List<String> results = null;
            try {
                results = processFailedNotification.processQueryFailedNotification(name);
            } catch (NoSuchElementException e) {
                LOGGER.debug("", e);
            }
            if (results != null && !results.isEmpty()) {
                for (String result : results) {
                    notifications.put(new JSONObject(result));
                    LOGGER.debug("Completed query for failed notification for subscription(s): {}",
                            name);
                }
            }
        }
        return notifications;
    }
}
