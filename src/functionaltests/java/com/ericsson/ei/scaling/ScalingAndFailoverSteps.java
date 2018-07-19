package com.ericsson.ei.scaling;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

@Ignore
public class ScalingAndFailoverSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAndFailoverSteps.class);
    
    @Before("@SubscriptionTriggerScenario")
    public void beforeScenario() {
    }

    @After("@SubscriptionTriggerScenario")
    public void afterScenario() {
    }
    
    @Given("^two instances of Eiffel Intelligence$")
    public void multiple_eiffel_intelligence_instances() {
        LOGGER.debug("Two eiffel intelligence instances will start");
    }
}
