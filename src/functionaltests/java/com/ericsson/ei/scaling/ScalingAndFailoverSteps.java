package com.ericsson.ei.scaling;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.FunctionalTestBase;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class ScalingAndFailoverSteps extends FunctionalTestBase {

    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_test.json";
    
    @LocalServerPort
    private int port;
    private List<Integer> portList = new ArrayList<Integer>();

    @Autowired
    private MockMvc mockMvc;
    private List<MockMvc> mockMvcList = new ArrayList<MockMvc>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Before("@ScalingAndFailoverScenario")
    public void beforeScenario() {
    }

    @After("@ScalingAndFailoverScenario")
    public void afterScenario() {
    }

    @Given("^\"([0-9]+)\" additional instance(.*) of Eiffel Intelligence$")
    public void multiple_eiffel_intelligence_instances(int multiple, String plural) throws Exception {       
        LOGGER.debug("{} additional eiffel intelligence instance{} will start", multiple, plural);
        
        for(int i = 0; i < multiple ; i++) {
            portList.add(SocketUtils.findAvailableTcpPort());
        }
        
        for(int i = 0; i < multiple ; i++) {
            LOGGER.debug("Starting instance on port: {}", portList.get(i));
            SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(App.class);          
            
            System.setProperty("server.port", String.valueOf(portList.get(i)));
            System.setProperty("spring.jmx.default-domain", "eiffel-intelligence-"+i);
            
            WebApplicationContext appContext = (WebApplicationContext) appBuilder.run();
            mockMvcList.add(webAppContextSetup(appContext).build());
        }
        
        LOGGER.debug("Ports for all available Application instances");
        LOGGER.debug("Default instance: {}", port);
        for(int i = 0; i < multiple ; i++) {
            LOGGER.debug("Additional instance {}: {}", i+1, portList.get(i));
        }
        
        LOGGER.debug("Testing REST API response code on all available Application instances");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions").accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        LOGGER.debug("Default instance: {}", String.valueOf(result.getResponse().getStatus()));
        for(int i = 0; i < multiple ; i++) {
            MockMvc mockMvcInstance = mockMvcList.get(i);
            MvcResult resultInstance = mockMvcInstance.perform(requestBuilder).andReturn();
            LOGGER.debug("Additional instance {}: {}", i+1, String.valueOf(resultInstance.getResponse().getStatus()));
        }
    }
    
    @When("^\"([0-9]+)\" eiffel events are sent$")
    public void multiple_eiffel_intelligence_instances(int multiple) throws Exception {
        LOGGER.debug("About to send Eiffel events");
        sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH);
        List<String> missingEventIds = verifyEventsInDB(getEventsIdList());
        assertEquals("The following events are missing in mongoDB: " + missingEventIds.toString(), 0,
                missingEventIds.size());
        LOGGER.debug("Eiffel events sent");
    }
    
    /**
     * Events to send
     */
    @Override
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");
        return eventNames;
    }
}
