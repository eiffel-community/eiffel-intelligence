package com.ericsson.ei.flowtests;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowTestExternalComposition extends FlowTestBase {

     private static Logger log = LoggerFactory.getLogger(FlowTest.class);

        protected ArrayList<String> getEventNamesToSend() {
             ArrayList<String> eventNames = new ArrayList<>();
             eventNames.add("event_EiffelArtifactCreatedEvent_3");
             eventNames.add("event_EiffelCompositionDefinedEvent_4");
             eventNames.add("event_EiffelArtifactCreatedEvent_4");
             eventNames.add("event_EiffelCompositionDefinedEvent_5");
             eventNames.add("event_EiffelArtifactCreatedEvent_5");
             return eventNames;
        }
}
