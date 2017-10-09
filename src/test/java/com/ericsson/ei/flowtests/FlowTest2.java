package com.ericsson.ei.flowtests;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest2 extends FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest2.class);

    static private final String inputFilePath2 = "src/test/resources/AggregatedDocument2.json";

    protected ArrayList<String> getEventNamesToSend() {
        ArrayList<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");

        eventNames.add("event_EiffelArtifactCreatedEvent_1");
        eventNames.add("event_EiffelArtifactPublishedEvent_1");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_1");
        eventNames.add("event_EiffelTestCaseStartedEvent_1");
        eventNames.add("event_EiffelTestCaseFinishedEvent_1");

        return eventNames;
    }

    protected void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        int processedEvents = 0;
        while (processedEvents < eventsCount) {
            String countStr = System.getProperty("eiffel.intelligence.processedEventsCount");
            String waitingCountStr = System.getProperty("eiffel.intelligence.waitListEventsCount");
            if (waitingCountStr == null)
                waitingCountStr = "0";
            Properties props = admin.getQueueProperties(queue.getName());
            int messageCount = Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
            processedEvents = Integer.parseInt(countStr) - Integer.parseInt(waitingCountStr) - messageCount;
        }
    }

    protected void checkResult() {
        try {
            String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
            String expectedDocument = FileUtils.readFileToString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            assertEquals(expectedJson.toString().length(), actualJson.toString().length());
            String expectedDocument2 = FileUtils.readFileToString(new File(inputFilePath2));
            String document2 = objectHandler.findObjectById("ccce572c-c364-441e-abc9-b62fed080ca2");
            JsonNode expectedJson2 = objectmapper.readTree(expectedDocument2);
            JsonNode actualJson2 = objectmapper.readTree(document2);
            assertEquals(expectedJson2.toString().length(), actualJson2.toString().length());
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }
}
