package com.ericsson.ei.subscriptions.trigger;

import com.ericsson.ei.utils.FunctionalBase;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
// @TestExecutionListeners(listeners = {
// DependencyInjectionTestExecutionListener.class,
// SubscriptionTriggerSteps.class })
public class SubscriptionTriggerSteps extends FunctionalBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionTriggerSteps.class);

    @Given("^Subscription rest trigger is created$")
    public void subscription_rest_trigger_is_created() throws Throwable {
        LOGGER.debug("Creating subscription rest trigger");
        System.out.println("MongoDB port for " + this.getClass().getName() + " is: " + getMongoDbPort());
    }

    @Given("^Subscription mail trigger is created$")
    public void subscription_mail_trigger_is_created() throws Throwable {
        LOGGER.debug("Creating subscription mail trigger");
    }

    @Given("^Subscription rest authenticated trigger is created$")
    public void subscription_rest_authenticated_trigger_is_created() throws Throwable {
        LOGGER.debug("Subscription rest authenticated trigger is created");
    }

    @When("^I send events$")
    public void send_events() throws Throwable {
        LOGGER.debug("Sending events");
    }

    @Then("^Subscriptions were triggered$")
    public void check_subscriptions_were_triggered() throws Throwable {

    }

}
