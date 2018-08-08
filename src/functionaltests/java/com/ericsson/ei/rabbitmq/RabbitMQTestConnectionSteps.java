package com.ericsson.ei.rabbitmq;

import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.utils.AMQPBrokerManager;
import com.ericsson.ei.utils.FunctionalTestBase;

import com.ericsson.ei.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.apache.commons.lang3.StringUtils;
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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.user}")
    private String user;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.tlsVersion}")
    private String tlsVersion;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.durable}")
    private Boolean queueDurable;

    @Value("${rabbitmq.binding.key}")
    private String bindingKey;

    @Autowired
    private MockMvc mockMvc;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQTestConnectionSteps.class);
    private static final String EIFFEL_EVENTS = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";

    private AMQPBrokerManager amqpBroker;
    private final ConnectionFactory factory = new ConnectionFactory();
    private Connection connection;
    private Channel channel;

    @Given("^We are connected to message bus$")
    public void connect_to_message_bus() {
        amqpBroker = TestContextInitializer.getBrokerFromPool(Integer.parseInt(rabbitMQPort));
        createRabbitMQConnection();
        assertNotEquals(null, connection);
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
        TimeUnit.SECONDS.sleep(10);
        Boolean connected = isConnectedWithBroker();

        assertEquals("Expected connection to AMQP broker to have recovered",
                true, connected);
    }

    @Then("^I send some events$")
    public void send_some_events() throws Exception {
        LOGGER.debug("Sending eiffel events");
        TimeUnit.SECONDS.sleep(10); // sleeping to ensure broker is up


        List<String> eventNames = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
    }

    @Then("^Events are in waitlist")
    public void events_are_received() throws Exception {
        TimeUnit.SECONDS.sleep(5);

        int waitListSize = dbManager.waitListSize();
        LOGGER.debug("Waitlist size after sending events is " + waitListSize);
        assertNotEquals(0, waitListSize);
    }

    @After
    public void afterScenario() {
        LOGGER.debug("Shutting down AMQP broker after tests");
        amqpBroker.stopBroker();
        TestContextInitializer.removeBrokerFromPool(Integer.parseInt(rabbitMQPort));
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


    /***
     * This method is used to create a RabbitMq connection
     */
    public void createRabbitMQConnection() {
        factory.setHost(host);
        factory.setPort(Integer.parseInt(rabbitMQPort));
        factory.setAutomaticRecoveryEnabled(true);
        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            factory.setUsername(user);
            factory.setPassword(password);
        }
        if (StringUtils.isNotBlank(tlsVersion)) {
            if (tlsVersion.contains("default")) {
                LOGGER.info("Using default TLS version connection to RabbitMQ.");
                try {
                    factory.useSslProtocol();
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            } else {
                LOGGER.info("Using TLS version " + tlsVersion + " connection to RabbitMQ.");
                try {
                    factory.useSslProtocol("TLSv" + tlsVersion);
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            // Enables publisher acknowledgement on channel
            channel.confirmSelect();
            channel.exchangeDeclare(exchangeName, "topic", queueDurable);
        } catch (Exception e) {
            LOGGER.error("Failed to create RabbitMQ connection. Reason: ", e);
        }
    }

    /***
     * This method is responsible for checking if there is a connection to the broker
     * by publishing a message and wait for confirmation
     *
     * @return true or false if connection is established to broker
     * */
    public Boolean isConnectedWithBroker() {
        long stopTime = TimeUnit.SECONDS.toMillis(10000);

        try {
            channel.basicPublish(exchangeName, bindingKey, null, "message".getBytes());
            /*while (!channel.waitForConfirms(stopTime)){
                if (stopTime <= System.currentTimeMillis()){
                    break;
                }
            }*/
            return channel.waitForConfirms(stopTime);
        } catch (Exception e) {
            e.getMessage();
        }
        return false;
    }

}
