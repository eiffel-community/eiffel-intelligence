package com.ericsson.ei.threadingAndWaitlistRepeat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.waitlist.WaitListWorker;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@TestPropertySource(properties = {
        "threads.corePoolSize= 3",
        "threads.queueCapacity= 1",
        "threads.maxPoolSize= 4",
        "waitlist.collection.ttlValue: 60",
        "waitlist.initialDelayResend= 500",
        "waitlist.fixedRateResend= 1000",
        "spring.data.mongodb.database: ThreadingAndWaitlistRepeatSteps",
        "missedNotificationDataBaseName: ThreadingAndWaitlistRepeatSteps-missedNotifications",
        "rabbitmq.exchange.name: ThreadingAndWaitlistRepeatSteps-exchange",
        "rabbitmq.consumerName: ThreadingAndWaitlistRepeatStepsConsumer",
        "logging.level.com.ericsson.ei.waitlist=DEBUG", "logging.level.com.ericsson.ei.handlers.EventHandler=DEBUG" })

@Ignore
public class ThreadingAndWaitlistRepeatSteps extends FunctionalTestBase {

    private File tempLogFile;
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";

    @Value("${threads.corePoolSize}")
    private int corePoolSize;
    @Value("${threads.queueCapacity}")
    private int queueCapacity;
    @Value("${threads.maxPoolSize}")
    private int maxPoolSize;
    @Value("${waitlist.collection.ttlValue}")
    private int waitlistTtl;

    @Autowired
    WaitListWorker waitListWorker;

    @Autowired
    Environment environment;

    @Before("@ThreadingAndWaitlistRepeatScenario")
    public void beforeScenario() throws IOException {
        tempLogFile = File.createTempFile("logfile", ".tmp");
        tempLogFile.deleteOnExit();
        System.setOut(new PrintStream(tempLogFile));
    }

    @Given("^that eiffel events are sent$")
    public void that_eiffel_events_are_sent() throws Throwable {
        List<String> eventNamesToSend = getEventNamesToSend();
        eventManager.sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH, eventNamesToSend);
    }

    @Then("^waitlist should not be empty$")
    public void waitlist_should_not_be_empty() throws Throwable {
        TimeUnit.SECONDS.sleep(5);
        int waitListSize = dbManager.waitListSize();
        assertNotEquals(0, waitListSize);
    }

    @Given("^no event is aggregated$")
    public void no_event_is_aggregated() throws Throwable {
        boolean aggregatedObjectExists = dbManager.verifyAggregatedObjectExistsInDB();
        assertEquals("aggregatedObjectExists was true, should be false, ", false, aggregatedObjectExists);
    }

    @Then("^the waitlist will try to resend the events at given time interval$")
    public void the_waitlist_will_try_to_resend_the_events_at_given_time_interval() throws Throwable {
        TimeUnit.SECONDS.sleep(5);
        List<String> resentEvents = new ArrayList<>();
        List<String> lines = new ArrayList<>(Files.readAllLines(tempLogFile.toPath()));

        int waitlistId = waitListWorker.hashCode();
        String waitListIdPattern = "FROM WAITLIST: " + waitlistId;
        Pattern pattern = Pattern.compile("\\[EIFFEL EVENT RESENT " + waitListIdPattern + "\\] id:([a-zA-Z\\d-]+)");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group(1).equals("")) {
                if (!resentEvents.contains(matcher.group(1))) {
                    resentEvents.add(matcher.group(1));
                }
            }
        }
        assertEquals(getEventNamesToSend().size(), resentEvents.size());
    }

    @Then("^correct amount of threads should be spawned$")
    public void correct_amount_of_threads_should_be_spawned() throws Throwable {
        String port = environment.getProperty("local.server.port");
        String eventHandlerThreadIdPattern = String.format("Thread id (\\d+) spawned for EventHandler on port: %s",
                port);
        Pattern pattern = Pattern.compile(eventHandlerThreadIdPattern);
        List<String> threadIds = new ArrayList<>();
        List<String> lines = new ArrayList<>(Files.readAllLines(tempLogFile.toPath()));

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group(0).equals("")) {
                if (!threadIds.contains(matcher.group(1))) {
                    threadIds.add(matcher.group(1));
                }
            }
        }

        assertEquals(getEventNamesToSend().size() - queueCapacity, threadIds.size());
    }

    @Then("^after the time to live has ended, the waitlist should be empty$")
    public void after_the_time_to_live_has_ended_the_waitlist_should_be_empty() throws Throwable {
        long stopTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(waitlistTtl + 60);
        while (dbManager.waitListSize() > 0 && stopTime > System.currentTimeMillis()) {
            TimeUnit.MILLISECONDS.sleep(10000);
        }
        int waitListSize = dbManager.waitListSize();
        assertEquals(0, waitListSize);
    }

    /**
     * Events used in the aggregation.
     */
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        return eventNames;
    }
}
