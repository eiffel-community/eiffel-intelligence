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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.ei.status.Status;
import com.ericsson.ei.status.StatusData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;

import lombok.Setter;

@Component
public class StatusHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHandler.class);
    private static final int REQUIRED_AVAILABLE_CONNECTIONS = 1;
    private static final String INITIAL_DELAY_OF_FIRST_STATUS_UPDATE = "1000";
    private static final String INTERVAL_TO_RUN_STATUS_UPDATES = "30000";

    private StatusData statusData = new StatusData();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Setter
    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private RmqHandler rmqHandler;

    @Value("${er.url:#{null}}")
    private String erUrl;

    /**
     * Scheduled method to run status update on a given interval.
     */
    @Scheduled(initialDelayString = INITIAL_DELAY_OF_FIRST_STATUS_UPDATE, fixedRateString = INTERVAL_TO_RUN_STATUS_UPDATES)
    public void run() {
        updateCurrentStatuses();
        System.out.println("Executing scheduled task");
    }

    /**
     * This method returns a JsonNode containing all fields and values in the statusData
     *
     * @return statusData
     */
    public JsonNode getCurrentStatus() {
        return objectMapper.valueToTree(statusData);
    }

    /**
     * Method to check and update statuses of all dependent services.
     */
    private void updateCurrentStatuses() {
        LOGGER.debug("Updating statuses.");

        Status mongoDBStatus = getMongoDBStatus();
        statusData.setMongoDBStatus(mongoDBStatus);

        Status rabbitMQStatus = getRabbitMQStatus();
        statusData.setRabbitMQStatus(rabbitMQStatus);

        Status eventRepositoryStatus = getEventRepositoryStatus();
        statusData.setEventRepositoryStatus(eventRepositoryStatus);

        Status eiffelIntelligenceStatus = getEiffelIntelligenceStatus();
        statusData.setEiffelIntelligenceStatus(eiffelIntelligenceStatus);
    }

    /**
     * Returns the status of the existing mongoDB connection.
     *
     * @return status
     */
    private Status getMongoDBStatus() {
        Status status;

        try {
            MongoDatabase database = mongoDBHandler.getMongoClient().getDatabase("admin");
            Document serverStatus = database.runCommand(new Document("serverStatus", 1));
            Map connections = (Map) serverStatus.get("connections");
            status = evaluateAvailableConnections(connections);
        } catch (Exception e) {
            status = Status.UNAVAILABLE;
        }

        LOGGER.debug("MongoDB status is {}", status);
        return status;
    }

    /**
     * Determines whether the number of available connections exceeds the required available
     * connections and returns a status based on the result.
     *
     * @param connections
     * @return status
     */
    private Status evaluateAvailableConnections(Map<String, Integer> connections) {
        Status status;
        int availableConnections = connections.get("available");
        int current = connections.get("current");

        if (availableConnections >= REQUIRED_AVAILABLE_CONNECTIONS) {
            status = Status.AVAILABLE;
        } else {
            status = Status.UNAVAILABLE;

            LOGGER.error("Available connections in MongoDB is less than expected.\n"
                    + "Available connections: '{}' used connections: '{}' expected available connections: '{}'",
                    availableConnections, current, REQUIRED_AVAILABLE_CONNECTIONS);
        }

        return status;
    }

    /**
     * Returns the status of the existing rabbitMQ connection.
     *
     * @return
     */
    private Status getRabbitMQStatus() {
        Status status;

        boolean isActive = rmqHandler.getContainer().isActive();
        boolean isRunning = rmqHandler.getContainer().isRunning();
        LOGGER.debug("RabbitMQ is running '{}' is active '{}'.", isRunning, isActive);

        if (isActive && isRunning) {
            status = Status.AVAILABLE;
        } else {
            status = Status.UNAVAILABLE;
        }

        LOGGER.debug("RabbitMQ status is {}", status);
        return status;
    }

    /**
     * Returns the status of the EventRepository service.
     *
     * @return
     */
    private Status getEventRepositoryStatus() {
        Status status;
        if (StringUtils.isEmpty(erUrl)) {
            status = Status.DISABLED;
        } else {
            // /HealtCheck or /Status endpoint must be available in the service to check.
            // TODO: HTTPRequest should be made to the ER.
            status = Status.UNKNOWN;
        }

        LOGGER.debug("EventRepository status is {}", status);
        return status;
    }

    /**
     * Returns the calculated Eiffel Intelligence status.
     *
     * @return status
     */
    private Status getEiffelIntelligenceStatus() {
        Status status;
        boolean availability = getServiceAvailability();

        if (availability) {
            status = Status.AVAILABLE;
        } else {
            status = Status.UNAVAILABLE;
        }

        LOGGER.debug("Eiffel Intelligence status is {}", status);
        return status;
    }

    /**
     * Check availability on dependent services.
     *
     * @return
     */
    private boolean getServiceAvailability() {
        boolean availability;

        boolean mongoDBAvailable = statusData.getMongoDBStatus() == Status.AVAILABLE;
        boolean rabbitMQAvailable = statusData.getRabbitMQStatus() == Status.AVAILABLE;
        boolean eventRepositoryAvailable = statusData.getEventRepositoryStatus() != Status.DISABLED
                && statusData.getEventRepositoryStatus() == Status.AVAILABLE;

        // TODO: Add check for ER status, may currently only be DISABLED or UNKNOWN.
        availability = mongoDBAvailable && rabbitMQAvailable;
        return availability;
    }
}
