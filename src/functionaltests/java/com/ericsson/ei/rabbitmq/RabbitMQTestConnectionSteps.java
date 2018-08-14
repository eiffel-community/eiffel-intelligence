package com.ericsson.ei.rabbitmq;

import com.ericsson.ei.utils.AMQPBrokerManager;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestContextInitializer;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Ignore
public class RabbitMQTestConnectionSteps extends FunctionalTestBase {

    @Value("${rabbitmq.port}")
    private String rabbitMQPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQTestConnectionSteps.class);
    private static final String EIFFEL_EVENTS = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";

    private AMQPBrokerManager amqpBroker;

    @Given("^We are connected to message bus$")
    public void connect_to_message_bus() {
        amqpBroker = TestContextInitializer.getBroker(Integer.parseInt(rabbitMQPort));
        assertEquals("Expected message bus to be up",
                true, amqpBroker.isRunning);
    }

    @When("^Message bus goes down$")
    public void message_bus_goes_down() {
        LOGGER.debug("Shutting down message bus");
        amqpBroker.stopBroker();
        assertEquals("Expected message bus to be down",
                false, amqpBroker.isRunning);
    }

    @When("^Message bus is restarted$")
    public void message_bus_is_restarted() throws Exception {
        amqpBroker.startBroker();
    }

    @Then("^I can send events which are put in the waitlist$")
    public void can_send_events_which_are_put_in_the_waitlist() throws Exception {
        LOGGER.debug("Sending eiffel events");
        int waitListSize = 0;
        List<String> eventNames = getEventNamesToSend();
        long maxTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

        while (waitListSize != 4 && maxTime > System.currentTimeMillis()) {
            eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
            TimeUnit.SECONDS.sleep(2);
            waitListSize = dbManager.waitListSize();
        }
        assertEquals(4, waitListSize);
    }

    /**
     * This method collects all the event names of events we will send to the message bus.
     */
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        return eventNames;
    }

}
