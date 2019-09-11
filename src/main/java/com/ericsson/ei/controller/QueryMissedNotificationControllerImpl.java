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

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.hibernate.validator.constraints.SafeHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.controller.model.QueryResponseEntity;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the REST GET mechanism to extract the aggregated data on the basis of the SubscriptionName from the Missed Notification
 * Object.
 */
@Component
@CrossOrigin
@Api(value = "queryMissedNotification", tags = {"Missed notifications"})
public class QueryMissedNotificationControllerImpl implements QueryMissedNotificationController {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueryMissedNotificationControllerImpl.class);

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    /**
     * This method is responsible for the REST GET mechanism to extract the data on the basis of the SubscriptionName from the Missed Notification
     * Object.
     *
     * @param subscriptionName
     */
    @Override
    @ApiOperation(value = "Retrieve missed notifications", response = QueryResponse.class)
    public ResponseEntity<?> getQueryMissedNotifications(@RequestParam("SubscriptionName") final String subscriptionName,
            final HttpServletRequest httpRequest) {
        ObjectMapper mapper = new ObjectMapper();
        QueryResponse queryResponse = new QueryResponse();
        QueryResponseEntity queryResponseEntity = new QueryResponseEntity();
        try {
            List<String> response = processMissedNotification.processQueryMissedNotification(subscriptionName);
            if (!response.isEmpty()) {
                queryResponseEntity = mapper.readValue(response.get(0), QueryResponseEntity.class);
            }
            queryResponse.setQueryResponseEntity(queryResponseEntity);
            LOGGER.debug("The response is : {}", response.toString());
            if (processMissedNotification.deleteMissedNotification(subscriptionName)) {
                LOGGER.debug("Missed notification with subscription name {} was successfully removed from database", subscriptionName);
            }
            return new ResponseEntity<>(queryResponse, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to extract the data from the Missed Notification Object based on subscription name "
                    + subscriptionName + ".";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
