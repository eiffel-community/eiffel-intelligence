package com.ericsson.ei.flowtests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public abstract class AbstractFlowTest {

    private static Logger log = LoggerFactory.getLogger(AbstractFlowTest.class);

    @Autowired
    RMQContainer rmqContainer;


    public synchronized void flowTest() {
        log.error("RMQCONTAINER: " + rmqContainer);
        try {
            String queueName = rmqContainer.rmqHandler.getQueueName();
            Channel channel = rmqContainer.conn.createChannel();
            String exchangeName = "ei-poc-4";
            createExchange(exchangeName, queueName);

            List<String> eventNames = getEventNamesToSend();
            int eventsCount = eventNames.size();
            for(String eventName : eventNames) {
                JsonNode eventJson = rmqContainer.parsedJson.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchangeName, queueName,  null, event.getBytes());
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            checkResult();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public abstract void runFlowTest();

    protected void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(RMQContainer.cf);
        RMQContainer.admin = new RabbitAdmin(ccf);
        RMQContainer.queue = new Queue(queueName, false);
        RMQContainer.admin.declareQueue(RMQContainer.queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        RMQContainer.admin.declareExchange(exchange);
        RMQContainer.admin.declareBinding(BindingBuilder.bind(RMQContainer.queue).to(exchange).with("#"));
        ccf.destroy();
    }

    abstract List<String> getEventNamesToSend();

    // count documents that were processed
    private long countProcessedEvents(String database, String collection){
        MongoDatabase db = RMQContainer.mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    protected void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(rmqContainer.database, rmqContainer.event_map);
            log.info("processed: " + processedEvents + ", counted: "+eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }

    protected void checkResult() {
        String document = rmqContainer.objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
        String expectedDocument;
        try {
            expectedDocument = FileUtils.readFileToString(new File(RMQContainer.inputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            assertEquals("\nExpectedJson:\n" + expectedJson.toString() + "\nActual:\n" + actualJson.toString() + "\n", expectedJson.toString().length(), actualJson.toString().length());
        } catch (IOException e) {
            log.info(e.getMessage(),e);
        }
    }

}
