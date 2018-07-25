package com.ericsson.ei.scaling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.MessageCounter;
import com.ericsson.ei.utils.MultiOutputStream;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class ScalingAndFailoverSteps extends FunctionalTestBase {
    private static final String EVENT_DUMMY = "src/functionaltests/resources/scale_and_failover_dummy.json";
    
    @LocalServerPort
    private int port;
    private List<Integer> portList = new ArrayList<Integer>();

    @Autowired
    private MockMvc mockMvc;
    private List<MockMvc> mockMvcList = new ArrayList<MockMvc>();

    private int numberOfInstances;
    ByteArrayOutputStream baos;
    MultiOutputStream multiOutput;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Before("@ScalingAndFailoverScenario")
    public void beforeScenario() {
        System.setProperty("scaling.test","true");
        baos = new ByteArrayOutputStream();
        multiOutput = new MultiOutputStream(System.out, baos);
        PrintStream printStream = new PrintStream(multiOutput);
        System.setOut(printStream);
    }

    @After("@ScalingAndFailoverScenario")
    public void afterScenario() {
    }

    @Given("^\"([0-9]+)\" additional instance(.*) of Eiffel Intelligence$")
    public void multiple_eiffel_intelligence_instances(int multiple, String plural) throws Exception {
        LOGGER.debug("{} additional eiffel intelligence instance{} will start", multiple, plural);
        numberOfInstances = multiple + 1;
        MessageCounter.getInstance().setSize(numberOfInstances);
        
        portList.add(this.port);
        for(int i = 1; i < numberOfInstances ; i++) {
            portList.add(SocketUtils.findAvailableTcpPort());
        }
        
        mockMvcList.add(this.mockMvc);
        for(int i = 1; i < numberOfInstances ; i++) {
            LOGGER.debug("Starting instance on port: {}", portList.get(i));
            SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(App.class);          
            
            System.setProperty("server.port", String.valueOf(portList.get(i)));
            System.setProperty("spring.jmx.default-domain", "eiffel-intelligence-"+i);
            
            WebApplicationContext appContext = (WebApplicationContext) appBuilder.run();
            mockMvcList.add(webAppContextSetup(appContext).build());
        }
        
        LOGGER.debug("Ports for all available Application instances");
        for(int i = 0; i < numberOfInstances ; i++) {
            LOGGER.debug("Instance {}, Port: {}", i+1, portList.get(i));
            String property = "ei."+portList.get(i)+".index";
            System.setProperty(property, ""+i);
        }

        LOGGER.debug("Testing REST API response code on all available Application instances");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions").accept(MediaType.APPLICATION_JSON);
        for(int i = 0; i < numberOfInstances ; i++) {
            MockMvc mockMvcInstance = mockMvcList.get(i);
            MvcResult resultInstance = mockMvcInstance.perform(requestBuilder).andReturn();
            int code = resultInstance.getResponse().getStatus();
            LOGGER.debug("Instance {}, Code: {}", i+1, code);
            assertEquals("Bad response code on port " + portList.get(i), 200, code);
        }
    }
    
    @When("^\"([0-9]+)\" eiffel events are sent$")
    public void multiple_eiffel_intelligence_instances(int multiple) throws Exception {
        LOGGER.debug("{} eiffel events will be sent", multiple);
        String event = FileUtils.readFileToString(new File(EVENT_DUMMY), "UTF-8");
        List<String> eventsIdList = new ArrayList<String>();
        for(int i = 0; i < multiple ; i++) {
            String uuid = UUID.randomUUID().toString();
            eventsIdList.add(uuid);
            String eventWithUUID = event;
            eventWithUUID = eventWithUUID.replaceAll("\\{uuid\\}", uuid);
            sendEiffelEvent(eventWithUUID);
        }
        List<String> missingEventIds = verifyEventsInDB(eventsIdList);
        assertEquals("Number of events missing in DB: " + missingEventIds.size(), 0,
                missingEventIds.size());
        LOGGER.debug("All eiffel events sent");
    }
    
    @Then("^event messages are evenly distributed$")
    public void event_messages_evenly_distributed() throws Exception {       
        double[] doubleArray = new double[numberOfInstances];
        for(int i = 0; i < numberOfInstances ; i++) {
            doubleArray[i] = MessageCounter.getInstance().getCount(i);
            LOGGER.debug("Port: {}, Message Count: {}", portList.get(i), (int)doubleArray[i]);
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics(doubleArray);
        double standardDeviation = stats.getStandardDeviation();
        double mean = stats.getMean();
        double coefficientOfVariation = standardDeviation/mean;
        LOGGER.debug("StandardDeviation: {}, Mean: {}, CV: {}", standardDeviation, mean, coefficientOfVariation);
        assertEquals("Coefficient of variation is abnormally high", coefficientOfVariation<1, true);
    }
}
