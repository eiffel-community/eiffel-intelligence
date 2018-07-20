package com.ericsson.ei.threadingAndWaitlistRepeat;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.utils.FunctionalTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.derby.tools.sysinfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Ignore;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;

import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@TestPropertySource(locations="classpath:test.properties")
public class ThreadingAndWaitlistRepeatSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadingAndWaitlistRepeatSteps.class);
    private static final String LOGFILE = "src/functionaltests/resources/logfile.txt";

    private static final String EIFFEL_EVENTS_JSON_PATH = "src/functionaltests/resources/eiffel_events_for_thread_testing.json";

    @Value("${threads.corePoolSize}") private int corePoolSize;
    @Value("${threads.queueCapacity}") private int queueCapacity;
    @Value("${threads.maxPoolSize}") private int maxPoolSize;

    @Before("@ThreadingAndWaitlistRepeatScenario")
    public void beforeScenario() throws IOException {
        File logfile = new File(LOGFILE);
        logfile.createNewFile();
        System.setOut(new PrintStream(logfile));
    }

    @Given("^that eiffel events are sent$")
    public void that_eiffel_events_are_sent() throws Throwable {
        sendEiffelEvents(EIFFEL_EVENTS_JSON_PATH);
        // How do we know they are sent?
    }

    @Given("^no event is aggregated$")
    public void no_event_is_aggregated() throws Throwable {
        boolean aggregatedObjectExists = verifyAggregatedObjectExistsInDB();
        assertEquals("aggregatedObjectExists was true, should be false, "
                , false, aggregatedObjectExists);
    }

    @Then("^waitlist should not be empty$")
    public void waitlist_should_not_be_empty() throws Throwable {
        TimeUnit.MILLISECONDS.sleep(1000);
        int waitListSize = waitListSize();
        assertNotEquals(0, waitListSize);
    }

    @Then("^the events should be resent at given time interval$")
    public void the_events_should_be_resent_at_given_time_interval() throws Throwable {
        // WaitListWorker logs every time an event is resent from the waitlist.
        TimeUnit.MILLISECONDS.sleep(5000);
        List<String> resentEvents = new ArrayList<String>();
        List<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(LOGFILE)));

        for (String line: lines) {
            Pattern pattern = Pattern.compile("\\[EIFFEL EVENT RESENT\\] id:([a-zA-Z\\d-]+)");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find() && !matcher.group(1).equals("")) {
                if (!resentEvents.contains(matcher.group(1))) {
                    resentEvents.add(matcher.group(1));
                }
            }
        }

        assertEquals(resentEvents.size(), 4);
    }

    @Then("^correct amount of threads should be spawned$")
    public void correct_amount_of_threads_should_be_spawned() throws Throwable {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

        int numberOfThreads = 0;
        for (Thread thread: threadArray) {
            System.out.println("Threadname: " + thread.getName());
            if (thread.getName().contains("EventHandler-")) {
                numberOfThreads += 1;
            }
        }

        assertEquals(3, numberOfThreads);
    }

    @Then("^after the time to live has ended, the waitlist should be empty$")
    public void after_the_time_to_live_has_ended_the_waitlist_should_be_empty() throws Throwable {
        TimeUnit.MILLISECONDS.sleep(130000);
        int waitListSize = waitListSize();
        assertEquals(0, waitListSize);
    }

    /**
     * Events used in the aggregation.
     */
    @Override
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        return eventNames;
    }
}