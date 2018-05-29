package com.ericsson.ei.subscriptions.crud;

import com.ericsson.ei.utils.FunctionalBase;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
public class SubscriptionCRUDSteps extends FunctionalBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCRUDSteps.class);

    @Given("^Subscription is created$")
    public void subscription_is_created() throws Throwable {
        LOGGER.debug("Creating subscription");
        System.out.println("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @When("^Update the subscription$")
    public void update_the_subscription() throws Throwable {
        LOGGER.debug("Updating subscription");
    }

    @Then("^Delete the updated subscription$")
    public void delete_the_updated_subscription() throws Throwable {

    }
}
