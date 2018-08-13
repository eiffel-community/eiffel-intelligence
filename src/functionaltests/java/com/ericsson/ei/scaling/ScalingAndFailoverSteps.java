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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.FunctionalTestBase;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Given("^(\\d+) additional instance(s)? of Eiffel Intelligence$")
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

    @When("^(\\d+) event messages are sent$")
    public void multiple_events(int multiple) throws Exception {
        LOGGER.debug("{} eiffel events will be sent", multiple);
        String event = FileUtils.readFileToString(new File(EVENT_DUMMY), "UTF-8");
        for (int i = 0; i < multiple; i++) {
            String uuid = UUID.randomUUID().toString();
            eventsIdList.add(uuid);
            String eventWithUUID = event;
            eventWithUUID = eventWithUUID.replaceAll("\\{uuid\\}", uuid);
            eventManager.sendEiffelEvent(eventWithUUID);
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
        List<String> missingEventIds = dbManager.verifyEventsInDB(eventsIdList);
        LOGGER.debug("Missing events: {}", missingEventIds.toString());
        assertEquals("Number of events missing in DB: " + missingEventIds.size(), 0, missingEventIds.size());
    }
}