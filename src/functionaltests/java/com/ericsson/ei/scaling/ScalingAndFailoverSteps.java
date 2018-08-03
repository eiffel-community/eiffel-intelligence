package com.ericsson.ei.scaling;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.MultiOutputStream;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@TestPropertySource(properties = { "logging.level.com.ericsson.ei.handlers.EventHandler=DEBUG" })
@Ignore
@AutoConfigureMockMvc
public class ScalingAndFailoverSteps extends FunctionalTestBase {
    private static final String EVENT_DUMMY = "src/functionaltests/resources/scale_and_failover_dummy.json";

    @LocalServerPort
    private int port;
    private List<Integer> portList = new ArrayList<>();

    private List<WebApplicationContext> appContextList = new ArrayList<>();

    @Autowired
    private MockMvc mockMvc;
    private List<MockMvc> mockMvcList = new ArrayList<>();

    private List<String> eventsIdList = new ArrayList<>();
    private int numberOfInstances;
    private ByteArrayOutputStream baos;
    private PrintStream printStream;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Before("@ScalingAndFailoverScenario")
    public void beforeScenario() {
        baos = new ByteArrayOutputStream();
        MultiOutputStream multiOutput = new MultiOutputStream(System.out, baos);
        printStream = new PrintStream(multiOutput);
        System.setOut(printStream);
    }

    @After("@ScalingAndFailoverScenario")
    public void afterScenario() {
        printStream.close();
    }

    @Given("^\"([0-9]+)\" additional instance(s)? of Eiffel Intelligence$")
    public void additional_eiffel_intelligence_instances(int multiple, String plural) {
        LOGGER.debug("{} additional eiffel intelligence instance{} will start", multiple, plural);
        numberOfInstances = multiple + 1;

        portList.add(this.port);
        for (int i = 1; i < numberOfInstances; i++) {
            portList.add(SocketUtils.findAvailableTcpPort());
        }

        mockMvcList.add(this.mockMvc);
        for (int i = 1; i < numberOfInstances; i++) {
            LOGGER.debug("Starting instance on port: {}", portList.get(i));
            SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(App.class);

            System.setProperty("server.port", String.valueOf(portList.get(i)));
            System.setProperty("spring.jmx.default-domain", "eiffel-intelligence-" + i);

            WebApplicationContext appContext = (WebApplicationContext) appBuilder.run();
            appContextList.add(appContext);

            mockMvcList.add(webAppContextSetup(appContext).build());
        }

        LOGGER.debug("Ports for all available instances");
        for (int i = 0; i < numberOfInstances; i++) {
            LOGGER.debug("Instance {}, Port: {}", i + 1, portList.get(i));
        }
    }

    @Given("^instances are up and running$")
    public void instances_running() throws Exception {
        LOGGER.debug("Testing REST API response code on all available instances");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions").accept(MediaType.APPLICATION_JSON);
        for (int i = 0; i < numberOfInstances; i++) {
            MockMvc mockMvcInstance = mockMvcList.get(i);
            MvcResult resultInstance = mockMvcInstance.perform(requestBuilder).andReturn();
            int code = resultInstance.getResponse().getStatus();
            LOGGER.debug("Instance {}, Code: {}", i + 1, code);
            assertEquals("Bad response code on port " + portList.get(i), 200, code);
        }
    }

    @When("^\"([0-9]+)\" event messages are sent$")
    public void multiple_events(int multiple) throws Exception {
        LOGGER.debug("{} eiffel events will be sent", multiple);
        String event = FileUtils.readFileToString(new File(EVENT_DUMMY), "UTF-8");
        for (int i = 0; i < multiple; i++) {
            String uuid = UUID.randomUUID().toString();
            eventsIdList.add(uuid);
            String eventWithUUID = event;
            eventWithUUID = eventWithUUID.replaceAll("\\{uuid\\}", uuid);
            sendEiffelEvent(eventWithUUID);
        }
    }

    @When("^additional instances are closed$")
    public void additional_instances_closed() {
        for (int i = 0; i < numberOfInstances - 1; i++) {
            ((ConfigurableApplicationContext) appContextList.get(i)).close();
            LOGGER.debug("Closed Application running on port {}", portList.get(i + 1));
        }
    }

    @Then("^all event messages are processed$")
    public void messages_processed() throws Exception {
        List<String> missingEventIds = verifyEventsInDB(eventsIdList);
        LOGGER.debug("Missing events: {}", missingEventIds.toString());
        assertEquals("Number of events missing in DB: " + missingEventIds.size(), 0, missingEventIds.size());
    }

    @Then("^event messages are unaffected by instance failure$")
    public void events_failover() throws Exception {
        List<Integer> receivedCount = new ArrayList<>();
        List<Integer> processedCount = new ArrayList<>();

        receivedCount = eventMessageCounter("received");
        processedCount = eventMessageCounter("processed");

        for (int i = 0; i < numberOfInstances; i++) {
            LOGGER.debug("Received, Instance {}, Port: {}, Message count: {}", i + 1, portList.get(i),
                    receivedCount.get(i));
            LOGGER.debug("Processed, Instance {}, Port: {}, Message count: {}", i + 1, portList.get(i),
                    processedCount.get(i));
        }
        int receivedTotal = receivedCount.stream().mapToInt(Integer::intValue).sum();
        int processedTotal = processedCount.stream().mapToInt(Integer::intValue).sum();

        LOGGER.debug("Total received message count: {}, Total processed message count: {}", receivedTotal,
                processedTotal);
        assertEquals("Total received messages is lower than the total processed count", true,
                receivedTotal >= processedTotal);
    }

    /**
     * Counts events received or processed by EventHandler
     *
     * @param type
     *            either received or processed
     * @return list of event occurrences
     */
    private List<Integer> eventMessageCounter(String type) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < numberOfInstances; i++) {
            String match = type + " on port " + portList.get(i);
            list.add(logCounter(match));
        }
        return list;
    }

    /**
     * Counts occurrence of matching string in log output
     *
     * @param match
     *            string to match with
     * @return amount of times string matched
     */
    private int logCounter(String match) {
        String consoleLog = baos.toString();
        int index = consoleLog.indexOf(match);
        int count = 0;
        while (index != -1) {
            count++;
            consoleLog = consoleLog.substring(index + 1);
            index = consoleLog.indexOf(match);
        }
        return count;
    }
}