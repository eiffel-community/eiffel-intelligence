package com.ericsson.ei.flowtests;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest extends FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    protected ArrayList<String> getEventNamesToSend() {
         ArrayList<String> eventNames = new ArrayList<>();
         eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
         eventNames.add("event_EiffelArtifactPublishedEvent_3");
         eventNames.add("event_EiffelArtifactCreatedEvent_3");
         eventNames.add("event_EiffelTestCaseStartedEvent_3");
         eventNames.add("event_EiffelTestCaseFinishedEvent_3");

         return eventNames;
    }
}
