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
package com.ericsson.ei.status;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.handlers.StatusHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: StatusSteps",
        "failed.notifications.collection.name: StatusSteps-missedNotifications",
        "rabbitmq.exchange.name: StatusSteps-exchange",
        "rabbitmq.queue.name: StatusSteps-queue" })
public class StatusTestSteps extends FunctionalTestBase {

    private static final String RABBITMQ_KEY = "rabbitMQStatus";
    private static final String MONGODB_KEY = "mongoDBStatus";

    private JsonNode fetchedStatus;

    @Autowired
    private StatusHandler statusHandler;

    @Given("^\"([^\"]*)\" service is unavailable$")
    public void setServiceStatusUnavailable(String service) throws Throwable {
        setUnavailableService(service);
        statusHandler.run();
    }

    @When("^I get status$")
    public void getServiceStatus() throws Throwable {
        fetchedStatus = statusHandler.getCurrentStatus();
    }

    @Then("^Verify \"([^\"]*)\" status is \"([^\"]*)\"$")
    public void verifyServiceStatus(String service, String status) throws Throwable {
        String message = String.format("The '%s' status should be '%s'.", service, status);
        assertEquals(message, status, fetchedStatus.get(service).asText());
    }

    private void setUnavailableService(String service) {
        switch (service) {
        case MONGODB_KEY:
            terminateMongoDB();
            break;
        case RABBITMQ_KEY:
            terminateRabbitMQ();
            break;
        default:
            return;
        }
    }

    private void terminateMongoDB() {
        // It is currently not possible to terminate MongoDB since EI does not reconnect and this
        // will fail all other tests.
    }

    private void terminateRabbitMQ() {
        eventManager.getRmqHandler().getCachingConnectionFactory().destroy();

    }
}
