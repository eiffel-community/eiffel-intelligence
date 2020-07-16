package com.ericsson.ei.rabbitmq.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.utils.AMQPBrokerManager;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.mongodb.BasicDBObject;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@TestPropertySource(properties = {
        "spring.data.mongodb.database: RabbitMQConfigurationTestSteps",
        "bindingkeys.collection.name: RabbitMQConfigurationTestSteps-bindingKeys",
        "missedNotificationDataBaseName: RabbitMQConfigurationTestSteps-missedNotifications",
        "rabbitmq.exchange.name: RabbitMQConfigurationTestSteps-exchange",
        "rabbitmq.consumerName: RabbitMQConfigurationTestStepsConsumer" })
public class RabbitMQConfigurationTestSteps extends FunctionalTestBase {

    @Value("${rabbitmq.port}")
    private String rabbitMQPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConfigurationTestSteps.class);
    private static final String EIFFEL_EVENTS = "src/functionaltests/resources/eiffel_events_for_test.json";

    private static final String ROUTING_KEY_1 = "routing-key-1";
    private static final String ROUTING_KEY_2 = "routing-key-2";

    private static final String BINDING_KEY_1 = "binding-key-1";
    private static final String BINDING_KEY_2 = "binding-key-2";

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
        
        RmqHandler rmqHandler = eventManager.getRmqHandler();
        rmqHandler.setPort(port);
        rmqHandler.connectionFactory();
        rmqHandler.getCachingConnectionFactory().createConnection();

        RabbitAdmin rabbitAdmin = createExchange(rmqHandler);
        RabbitTemplate rabbitTemplate = rabbitAdmin.getRabbitTemplate();

        rmqHandler.setRabbitTemplate(rabbitTemplate);
        rmqHandler.getContainer().setRabbitAdmin(rabbitAdmin);
        rmqHandler.getContainer().setConnectionFactory(rmqHandler.getCachingConnectionFactory());
        rmqHandler.getContainer().setQueueNames(rmqHandler.getQueueName());
        assertEquals("Expected message bus to be up", true, amqpBroker.isRunning);
    }

    @When("^events are published using different routing keys$")
    public void events_are_published_using_different_routing_keys() throws Exception {
        LOGGER.debug("Sending eiffel events");
        List<String> eventNames = getEventNamesToSend();
        eventNames.remove(1);
        eventManager.getRmqHandler().rabbitMqTemplate().setRoutingKey(ROUTING_KEY_1);
        eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
        eventNames.clear();
        eventNames = getEventNamesToSend();
        eventNames.remove(0);
        eventManager.getRmqHandler().rabbitMqTemplate().setRoutingKey(ROUTING_KEY_2);
        eventManager.sendEiffelEvents(EIFFEL_EVENTS, eventNames);
    }

    @Then("^an aggregated object should be created$")
    public void an_aggregated_object_should_be_created() throws Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add("_id=6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
        arguments.add("uri=https://myrepository.com/mySubSystemArtifact");
        List<String> missingArguments = dbManager.verifyAggregatedObjectInDB(arguments);
        assertEquals("The following arguments are missing in the Aggregated Object in mongoDB: "
                + missingArguments.toString(), 0, missingArguments.size());
    }

    @When("^add the binding documents to mongoDB$")
    public void add_the_binding_documents_to_mongoDB() {
        BasicDBObject dbBinding = insertBinding();
        assertNotNull(dbBinding);
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
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        return eventNames;
    }

    private RabbitAdmin createExchange(final RmqHandler rmqHandler) {
        final String exchangeName = rmqHandler.getExchangeName();
        final String queueName = rmqHandler.getQueueName();
        final CachingConnectionFactory ccf = rmqHandler.getCachingConnectionFactory();
        LOGGER.info("Creating exchange: {} and queue: {}", exchangeName, queueName);
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, true);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_1));
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_2));
        admin.initialize();
        admin.getQueueProperties(queueName);
        RabbitTemplate rabbitTemplate = admin.getRabbitTemplate();
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setQueue(queueName);
        rabbitTemplate.setRoutingKey(ROUTING_KEY_1);
        return admin;
    }

    private BasicDBObject insertBinding() {
        BasicDBObject docInput = new BasicDBObject();
        docInput.put("destination", "ei-domain.eiffel-intelligence.messageConsumer.durable");
        docInput.put("destinationType", "QUEUE");
        docInput.put("exchange", "ei-exchange");
        docInput.put("bindingKeys", BINDING_KEY_1);
        docInput.put("arg", null);
        mongoDBHandler.insertDocument(dataBaseName, collectionName, docInput.toString());
        return docInput;
    }

    private ArrayList<String> compareAndRemoveBindings() {
        ArrayList<Binding> listBinding = new ArrayList<Binding>();
        listBinding.add(new Binding("ei-domain.eiffel-intelligence.messageConsumer.durable", DestinationType.QUEUE,
                "ei-exchange", BINDING_KEY_2, null));
        List<String> allObjects = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        ArrayList<String> removedBinding = new ArrayList<String>();

        JSONObject dbBinding = new JSONObject(allObjects.get(0));
        String mongoDbBinding = dbBinding.getString("bindingKeys");
        if (!(listBinding.contains(mongoDbBinding))) {
            removedBinding.add(mongoDbBinding);
            listBinding.remove(0);
            String condition = "{\"bindingKeys\": /.*" + mongoDbBinding + "/}";
            mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
        }
        return removedBinding;
    }

}