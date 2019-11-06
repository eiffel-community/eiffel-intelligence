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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class contains logic for retrieving failed notifications for the given subscription.
 */
@Component
@CrossOrigin
@Api(value = "failedNotifications", tags = { "Failed notifications" })
public class FailedNotificationControllerImpl implements FailedNotificationController {

    private final static Logger LOGGER = LoggerFactory.getLogger(
            FailedNotificationControllerImpl.class);

    @Autowired
    private ProcessFailedNotification processMissedNotification;

    /**
     * This method is responsible for the REST GET mechanism to extract the data on the basis of the
     * subscription name from the Failed Notification Object.
     *
     * @param subscriptionName
     */
    @Override
    @ApiOperation(value = "Retrieve failed notifications", response = QueryResponse.class)
    public ResponseEntity<?> getFailedNotifications(
            @RequestParam(value = "subscriptionName", required = true) final String subscriptionName,
            final HttpServletRequest httpRequest) {
        Set<String> subscriptionNameList = new HashSet<>(
                Arrays.asList(subscriptionName.split(",")));
        JSONArray foundArray = new JSONArray();
        List<String> notFoundList = new ArrayList<>();

        subscriptionNameList.forEach(name -> {
            try {
                List<String> response = processMissedNotification.processQueryFailedNotification(
                        name);
                if (!response.isEmpty()) {
                    JSONObject object = new JSONObject(response.get(0));
                    foundArray.put(object);
                    LOGGER.debug("Successfully fetched failed notification for subscription [{}]",
                            name);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to fetch failed notification for subscription {}", name, e);
                notFoundList.add(name);
            }
        });
        HttpStatus httpStatus;
        if (foundArray.length() > 0) {
            httpStatus = HttpStatus.OK;
        } else {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        if (httpStatus == HttpStatus.NOT_FOUND) {
            String errorMessage = "Failed to fetch failed notifications for subscriptions:\n"
                    + notFoundList.toString();
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, httpStatus);
        }
        return new ResponseEntity<>(foundArray.toString(), httpStatus);
    }
}
