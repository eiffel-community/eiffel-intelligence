package com.ericsson.ei.threadingAndWaitlistRepeat;

import com.ericsson.ei.utils.FunctionalTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@TestPropertySource(properties = { "threads.corePoolSize= 3", "threads.queueCapacity= 1", "threads.maxPoolSize= 4",
        "waitlist.collection.ttlValue: 60", "waitlist.initialDelayResend= 500", "waitlist.fixedRateResend= 1000",
        "logging.level.com.ericsson.ei.waitlist=DEBUG" })

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

        for (String line : lines) {
            Pattern pattern = Pattern.compile("\\[EIFFEL EVENT RESENT\\] id:([a-zA-Z\\d-]+)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group(1).equals("")) {
                if (!resentEvents.contains(matcher.group(1))) {
                    resentEvents.add(matcher.group(1));
                }
            }
        }
        assertEquals(resentEvents.size(), getEventNamesToSend().size());
    }

    @Then("^correct amount of threads should be spawned$")
    public void correct_amount_of_threads_should_be_spawned() throws Throwable {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

        int numberOfThreads = 0;
        for (Thread thread : threadArray) {
            if (thread.getName().contains("EventHandler-")) {
                numberOfThreads += 1;
            }
        }
        assertEquals(getEventNamesToSend().size() - queueCapacity, numberOfThreads);
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
