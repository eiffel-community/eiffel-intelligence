/*
   Copyright 2019 Ericsson AB.
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
package com.ericsson.ei.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.ei.listeners.RMQConnectionListener;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.status.Status;
import com.ericsson.ei.status.StatusData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class updates the statuses of EI, RabbitMQ and MongoDB on a given timer interval. When
 * requested it returns status as a JsonNode.
 *
 */
@Component
public class StatusHandler {

    private static final String INITIAL_DELAY_OF_FIRST_STATUS_UPDATE = "1000";
    private static final String INTERVAL_TO_RUN_STATUS_UPDATES = "5000";

    private StatusData statusData = new StatusData();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private RMQConnectionListener rmqConnectionListener = new RMQConnectionListener();

    /**
     * Scheduled method to run status update on a given interval.
     */
    @Scheduled(initialDelayString = INITIAL_DELAY_OF_FIRST_STATUS_UPDATE, fixedRateString = INTERVAL_TO_RUN_STATUS_UPDATES)
    public void run() {
        updateCurrentStatus();
    }

    /**
     * This method returns a JsonNode containing all fields and values in the statusData
     *
     * @return statusData
     */
    public JsonNode getCurrentStatus() {
        return objectMapper.valueToTree(statusData);
    }

    private void updateCurrentStatus() {
        Status mongoDBStatus = getMongoDBStatus();
        statusData.setMongoDBStatus(mongoDBStatus);

        Status rabbitMQStatus = getRabbitMQStatus();
        statusData.setRabbitMQStatus(rabbitMQStatus);

        Status eiffelIntelligenceStatus = getEiffelIntelligenceStatus();
        statusData.setEiffelIntelligenceStatus(eiffelIntelligenceStatus);
    }

    private Status getMongoDBStatus() {
        Status status = Status.UNAVAILABLE;

        if (mongoDBHandler.isMongoDBServerUp()) {
            status = Status.AVAILABLE;
        }

        return status;
    }

    private Status getRabbitMQStatus() {
        Status status = Status.UNAVAILABLE;

        if (rmqConnectionListener.isConnected()) {
            status = Status.AVAILABLE;
        }

        return status;
    }

    private Status getEiffelIntelligenceStatus() {
        Status status = Status.UNAVAILABLE;
        boolean availability = getServiceAvailability();

        if (availability) {
            status = Status.AVAILABLE;
        }

        return status;
    }

    private boolean getServiceAvailability() {
        boolean availability;

        boolean mongoDBAvailable = statusData.getMongoDBStatus() == Status.AVAILABLE;
        boolean rabbitMQAvailable = statusData.getRabbitMQStatus() == Status.AVAILABLE;

        availability = mongoDBAvailable && rabbitMQAvailable;
        return availability;
    }
}
