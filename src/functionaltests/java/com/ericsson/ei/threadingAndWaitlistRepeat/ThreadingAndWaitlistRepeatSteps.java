package com.ericsson.ei.threadingAndWaitlistRepeat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        "rabbitmq.consumerName: ThreadingAndWaitlistRepeatStepsConsumer", "logging.level.com.ericsson.ei.waitlist=OFF",
        "logging.level.com.ericsson.ei.handlers.EventHandler=OFF" })

@Ignore
public class ThreadingAndWaitlistRepeatSteps extends FunctionalTestBase {
    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";
    private static final String ID_RULE = "{" + "\"IdRule\": \"meta.id\"" + "}";

    @Autowired
    private Environment environment;

    @Value("${threads.corePoolSize}")
    private int corePoolSize;
    @Value("${threads.queueCapacity}")
    private int queueCapacity;
    @Value("${threads.maxPoolSize}")
    private int maxPoolSize;
    @Value("${waitlist.collection.ttlValue}")
    private int waitlistTtl;

    private RulesObject rulesObject;

    private JsonNode rulesJson;

    @Autowired
    EventToObjectMapHandler eventToObjectMapHanler;

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

    @Then("^event-to-object-map is manipulated to include the sent events$")
    public void event_to_object_map_is_manipulated_to_include_the_sent_events() throws Throwable {
        JsonNode parsedJSON = eventManager.getJSONFromFile(EIFFEL_EVENTS_JSON_PATH);
        ObjectMapper objectMapper = new ObjectMapper();
        rulesJson = objectMapper.readTree(ID_RULE);
        rulesObject = new RulesObject(rulesJson);

        String dummyObjectID = "1234abcd-12ab-12ab-12ab-123456abcdef";
        List<String> eventNames = getEventNamesToSend();
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            eventToObjectMapHanler.updateEventToObjectMapInMemoryDB(rulesObject, eventJson.toString(), dummyObjectID);
        }
    }

    @Then("^when waitlist has resent events they should have been deleted$")
    public void when_waitlist_has_resent_events_they_should_have_been_deleted() throws Throwable {
        long stopTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
        while (dbManager.waitListSize() > 0 && stopTime > System.currentTimeMillis()) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        assertEquals("Waitlist resent events and due their presence in event-to-object-map, events are deleted", 0,
                dbManager.waitListSize());
    }

    @Then("^correct amount of threads should be spawned$")
    public void correct_amount_of_threads_should_be_spawned() throws Throwable {
        List<String> threadsSpawned = new ArrayList<>();
        String port = environment.getProperty("local.server.port");
        String eventHandlerThreadNamePattern = String.format("EventHandler-(\\d+)-%s", port);
        Pattern pattern = Pattern.compile(eventHandlerThreadNamePattern);

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread thread : threadArray) {
            Matcher matcher = pattern.matcher(thread.getName());
            if (matcher.find() && !matcher.group(1).equals("")) {
                if (!threadsSpawned.contains(matcher.group(1))) {
                    threadsSpawned.add(matcher.group(1));
                }
            }
        }
        assertEquals(getEventNamesToSend().size() - queueCapacity, threadsSpawned.size());
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
