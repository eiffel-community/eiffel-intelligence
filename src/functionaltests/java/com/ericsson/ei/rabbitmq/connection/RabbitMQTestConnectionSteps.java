package com.ericsson.ei.rabbitmq.connection;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.handlers.RMQHandler;
import com.ericsson.ei.handlers.RMQProperties;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.mongo.MongoStringQuery;
import com.ericsson.ei.utils.AMQPBrokerManager;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.mongodb.BasicDBObject;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: RabbitMQTestConnectionSteps",
        "bindingkeys.collection.name: RabbitMQConfigurationTestSteps-bindingKeys",
        "failed.notifications.collection.name: RabbitMQTestConnectionSteps-failedNotifications",
        "rabbitmq.exchange.name: RabbitMQTestConnectionSteps-exchange",
        "rabbitmq.queue.suffix: RabbitMQTestConnectionSteps" })
public class RabbitMQTestConnectionSteps extends FunctionalTestBase {

    @Value("${rabbitmq.port}")
    private String rabbitMQPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQTestConnectionSteps.class);
    private static final String EIFFEL_EVENTS = "src/functionaltests/resources/eiffel_events_for_test.json";

    private static final String BINDING_KEY_1 = "binding-key-1";
    private static final String BINDING_KEY_2 = "binding-key-2";
    private static final String DEFAULT_ROUTING_KEY = "#";

    private AMQPBrokerManager amqpBroker;

    @Autowired
    @Qualifier("bindToQueueForRecentEvents")
    SimpleMessageListenerContainer container;

    @Autowired
    EventHandler eventHandler;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${bindingkeys.collection.name}")
    private String collectionName;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Given("^We are connected to message bus$")
    public void connect_to_message_bus() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();
        String config = "src/functionaltests/resources/configs/qpidConfig.json";
        File qpidConfig = new File(config);
        amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath(), port);
        amqpBroker.startBroker();

        RMQHandler rmqHandler = eventManager.getRmqHandler();
        RMQProperties rmqProperties = rmqHandler.getRmqProperties();
        rmqProperties.setPort(port);
        rmqHandler.connectionFactory();
        rmqHandler.getCachingConnectionFactory().createConnection();

        RabbitAdmin rabbitAdmin = createExchange(rmqHandler);
        RabbitTemplate rabbitTemplate = rabbitAdmin.getRabbitTemplate();

        rmqHandler.setRabbitTemplate(rabbitTemplate);
        //rmqHandler.getContainer().setRabbitAdmin(rabbitAdmin);
        rmqHandler.getContainer().setAmqpAdmin(rabbitAdmin);
        rmqHandler.getContainer().setConnectionFactory(rmqHandler.getCachingConnectionFactory());
        rmqHandler.getContainer().setQueueNames(rmqProperties.getQueueName());
        assertEquals("Expected message bus to be up", true, amqpBroker.isRunning);
    }

    @When("^Message bus goes down$")
    public void message_bus_goes_down() {
        LOGGER.debug("Shutting down message bus");
        amqpBroker.stopBroker();
        assertEquals("Expected message bus to be down", false, amqpBroker.isRunning);
    }

    @When("^Message bus is restarted$")
    public void message_bus_is_restarted() throws Exception {
        amqpBroker.startBroker();
        createExchange(eventManager.getRmqHandler());
    }

    @Then("^I can send events which are put in the waitlist$")
    public void can_send_events_which_are_put_in_the_waitlist() throws Exception {
        LOGGER.debug("Sending eiffel events");
        int waitListSize = 0;
        List<String> eventNames = getEventNamesToSend();
        long maxTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

        while (waitListSize != 4 && maxTime > System.currentTimeMillis()) {
            eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
            TimeUnit.SECONDS.sleep(2);
            waitListSize = dbManager.waitListSize();
        }
        assertEquals(1, waitListSize);
    }

    @When("^add the binding documents to mongoDB$")
    public void add_the_binding_documents_to_mongoDB() {
        BasicDBObject dbBinding = insertBinding();
        assertEquals(5,dbBinding.size());
    }

    @Then("^compare the binding keys and remove the old binding keys from rabbitMQ and mongoDB$")
    public void compare_the_binding_keys_and_remove_the_old_binding_keys_from_rabbitMQ_and_mongoDB() {
        LOGGER.debug("comparing the binding keys to remove the old binding key");
        ArrayList<String> removedBinding = compareAndRemoveBindings();
        assertEquals(1, removedBinding.size());
    }

    /**
     * This method collects all the event names of events we will send to the
     * message bus.
     */
    protected List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        return eventNames;
    }

    private RabbitAdmin createExchange(final RMQHandler rmqHandler) {
        RMQProperties rmqProperties = rmqHandler.getRmqProperties();
        final String exchangeName = rmqProperties.getExchangeName();
        final String queueName = rmqProperties.getQueueName();
        final CachingConnectionFactory ccf = rmqHandler.getCachingConnectionFactory();
        LOGGER.info("Creating exchange: {} and queue: {}", exchangeName, queueName);
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, true);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        admin.initialize();
        admin.getQueueProperties(queueName);
        RabbitTemplate rabbitTemplate = admin.getRabbitTemplate();
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(DEFAULT_ROUTING_KEY);
        //rabbitTemplate.setQueue(queueName);
        rabbitTemplate.setDefaultReceiveQueue(queueName);
        return admin;
    }

    private BasicDBObject insertBinding() {
        BasicDBObject docInput = new BasicDBObject();
        docInput.put("destination", "ei-domain.eiffel-intelligence.messageConsumer.durable");
        docInput.put("destinationType", "QUEUE");
        docInput.put("exchange", "ei-binding-keys");
        docInput.put("bindingKeys", BINDING_KEY_1);
        docInput.put("arg", null);
        mongoDBHandler.insertDocument(dataBaseName, collectionName, docInput.toString());
        return docInput;
    }

    private ArrayList<String> compareAndRemoveBindings() {
        ArrayList<Binding> listBinding = new ArrayList<Binding>();
        listBinding.add(new Binding("ei-domain.eiffel-intelligence.messageConsumer.durable", DestinationType.QUEUE,
                "ei-binding-keys", BINDING_KEY_2, null));
        List<String> allObjects = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        ArrayList<String> removedBinding = new ArrayList<String>();

        JSONObject dbBinding = new JSONObject(allObjects.get(0));
        String mongoDbBinding = dbBinding.getString("bindingKeys");
        if (!(listBinding.contains(mongoDbBinding))) {
            removedBinding.add(mongoDbBinding);
            listBinding.remove(0);
            final MongoCondition condition = MongoCondition.bindingKeyCondition(mongoDbBinding);
            mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
        }
        return removedBinding;
    }

}