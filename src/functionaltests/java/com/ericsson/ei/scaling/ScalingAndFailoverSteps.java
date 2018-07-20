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

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.FunctionalTestBase;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

@Ignore
@AutoConfigureMockMvc
public class ScalingAndFailoverSteps extends FunctionalTestBase {

    @LocalServerPort
    private int port;
    private List<Integer> portList = new ArrayList<Integer>();

    @Autowired
    private MockMvc mockMvc;
    private List<MockMvc> mockMvcList = new ArrayList<MockMvc>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);

    @Before("@SubscriptionTriggerScenario")
    public void beforeScenario() {
    }

    @After("@SubscriptionTriggerScenario")
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
}
