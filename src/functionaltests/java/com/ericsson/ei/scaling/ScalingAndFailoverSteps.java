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

@TestPropertySource(properties = { "logging.level.com.ericsson.ei.handlers=INFO" })
@Ignore
@AutoConfigureMockMvc
public class ScalingAndFailoverSteps extends FunctionalTestBase {
    private static final String EVENT_DUMMY = "src/functionaltests/resources/scale_and_failover_dummy.json";

    @LocalServerPort
    private int port;
    private List<Integer> portList = new ArrayList<Integer>();

    private List<WebApplicationContext> appContextList = new ArrayList<WebApplicationContext>();

    @Autowired
    private MockMvc mockMvc;
    private List<MockMvc> mockMvcList = new ArrayList<MockMvc>();

    private List<String> eventsIdList = new ArrayList<String>();
    private int numberOfInstances;
    private ByteArrayOutputStream baos;
    private MultiOutputStream multiOutput;
    private PrintStream printStream;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Before("@ScalingAndFailoverScenario")
    public void beforeScenario() {
        System.setProperty("scaling.test", "true");
        baos = new ByteArrayOutputStream();
        multiOutput = new MultiOutputStream(System.out, baos);
        printStream = new PrintStream(multiOutput);
        System.setOut(printStream);
    }

    @After("@ScalingAndFailoverScenario")
    public void afterScenario() {
        printStream.close();
    }

    @Given("^\"([0-9]+)\" additional instance(.*) of Eiffel Intelligence$")
    public void additional_eiffel_intelligence_instances(int multiple, String plural) throws Exception {
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
            String property = "ei." + portList.get(i) + ".index";
            System.setProperty(property, "" + i);
        }

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

    @When("^\"([0-9]+)\" eiffel events are sent$")
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
        for (int x = 0; x < numberOfInstances - 1; x++) {
            ((ConfigurableApplicationContext) appContextList.get(x)).close();
            LOGGER.debug("Closed Application running on port {}", portList.get(x + 1));
        }
    }

    @Then("^all event messages are processed$")
    public void messages_processed() throws Exception {
        List<String> missingEventIds = verifyEventsInDB(eventsIdList);
        LOGGER.debug("Missing events: {}", missingEventIds.toString());
        assertEquals("Number of events missing in DB: " + missingEventIds.size(), 0, missingEventIds.size());
    }

    @Then("^unprocessed events are affected by failover$")
    public void events_failover() throws Exception {
        List<Integer> receivedCount = new ArrayList<Integer>();
        List<Integer> processedCount = new ArrayList<Integer>();
        String match;

        int receivedTotal = 0;
        for (int i = 0; i < numberOfInstances; i++) {
            match = "received on port " + portList.get(i);
            int counter = logCounter(match);
            receivedTotal += counter;
            receivedCount.add(counter);
        }

        int processedTotal = 0;
        for (int i = 0; i < numberOfInstances; i++) {
            match = "processed on port " + portList.get(i);
            int counter = logCounter(match);
            processedTotal += counter;
            processedCount.add(counter);
        }

        for (int i = 0; i < numberOfInstances; i++) {
            LOGGER.debug("Received, Instance {}, Port: {}, Message count: {}", i + 1, portList.get(i),
                    receivedCount.get(i));
            LOGGER.debug("Processed, Instance {}, Port: {}, Message count: {}", i + 1, portList.get(i),
                    processedCount.get(i));
        }
        LOGGER.debug("Total received message count: {}, Total processed message count: {}", receivedTotal,
                processedTotal);
        assertEquals("No failover took place", true, receivedTotal > processedTotal);
        LOGGER.debug("Failover successfully took place");
    }

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