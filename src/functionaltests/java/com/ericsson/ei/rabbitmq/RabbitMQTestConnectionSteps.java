package com.ericsson.ei.rabbitmq;

import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.utils.AMQPBrokerManager;
import com.ericsson.ei.utils.FunctionalTestBase;

import com.ericsson.ei.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.junit.Assert;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Ignore
@AutoConfigureMockMvc
@TestPropertySource(properties = {"threads.corePoolSize= 3",
        "threads.queueCapacity= 1", "threads.maxPoolSize= 4",
        "logging.level.com.ericsson.ei.rabbitmq=DEBUG",
        "logging.level.com.ericsson.ei.utils=DEBUG",
        "logging.level.com.ericsson.ei.rmqhandler.RmqHandler=DEBUG",
        "logging.level.org.springframework.web=DEBUG"})
public class RabbitMQTestConnectionSteps extends FunctionalTestBase {

    @Value("${rabbitmq.port}")
    private String rabbitMQPort;

    @Autowired
    private MockMvc mockMvc;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQTestConnectionSteps.class);
    private static final String EIFFEL_EVENTS = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";


    @Given("^Eiffel Intelligence is up and running$")
    public void eiffel_intelligence_is_up_and_running() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get("/subscriptions")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        LOGGER.debug("amqp broker port " + TestContextInitializer.amqpBroker.getPort());

    }

    @When("^Message bus goes down$")
    public void message_bus_goes_down() {
        LOGGER.debug("Shutting down message bus");
        TestContextInitializer.amqpBroker.stopBroker(); // TODO: get from context instead?
        assertEquals("Expected message bus to be down",
                false, TestContextInitializer.amqpBroker.isRunning);
    }

    @When("^Message bus is restarted$")
    public void message_bus_is_restarted() throws Exception {
        TestContextInitializer.amqpBroker.startBroker(); // TODO: get from context instead?
        assertEquals("Expected AQMP broker to be running",
                true, TestContextInitializer.amqpBroker.isRunning);
    }

    @Then("^I send some events$")
    public void send_some_events() throws Exception {
        LOGGER.debug("Sending eiffel events");

        List<String> eventNames = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
    }

    @Then("^Events are received$")
    public void events_are_received() throws Exception {
        TimeUnit.SECONDS.sleep(5);
        int debug = 1;
        //TODO: verify events have been received and handled
        int waitListSize = dbManager.waitListSize();
        LOGGER.debug("Waitlist size after sending events is " + waitListSize);
        assertNotEquals(0, waitListSize);
    }

    @After
    public void afterScenario() {
        //TODO: remove amqpBroker from pool after all tests have run

    }


    /**
     * Events used in the aggregation.
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
