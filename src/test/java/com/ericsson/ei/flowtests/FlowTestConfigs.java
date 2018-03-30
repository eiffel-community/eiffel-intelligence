package com.ericsson.ei.flowtests;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;

public class FlowTestConfigs {

        @Autowired
        private MongoDBHandler mongoDBHandler;

        @Autowired
        private WaitListStorageHandler waitlist;

        private static AMQPBrokerManager amqpBroker;
        private static MongodForTestsFactory testsFactory;
        private static Queue queue = null;
        private static RabbitAdmin admin;
        private static ConnectionFactory cf;
        protected static Connection conn;
        protected static MongoClient mongoClient = null;

        @BeforeClass
        public static void setup() throws Exception {
                System.setProperty("flow.test", "true");
                System.setProperty("eiffel.intelligence.processedEventsCount", "0");
                System.setProperty("eiffel.intelligence.waitListEventsCount", "0");
                setUpMessageBus();
                setUpEmbeddedMongo();
        }

        @PostConstruct
        public void initMocks() {
                mongoDBHandler.setMongoClient(mongoClient);
                waitlist.setMongoDbHandler(mongoDBHandler);
        }

        private static void setUpMessageBus() throws Exception {
                System.setProperty("rabbitmq.port", "8672");
                System.setProperty("rabbitmq.user", "guest");
                System.setProperty("rabbitmq.password", "guest");
                System.setProperty("waitlist.initialDelayResend", "5000");
                System.setProperty("waitlist.fixedRateResend", "1000");

                String config = "src/test/resources/configs/qpidConfig.json";
                File qpidConfig = new File(config);
                amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
                amqpBroker.startBroker();
                cf = new ConnectionFactory();
                cf.setUsername("guest");
                cf.setPassword("guest");
                cf.setPort(8672);
                cf.setHandshakeTimeout(600000);
                cf.setConnectionTimeout(600000);
                conn = cf.newConnection();
        }

        private static void setUpEmbeddedMongo() throws Exception {
                testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
                mongoClient = testsFactory.newMongo();
                String port = "" + mongoClient.getAddress().getPort();
                System.setProperty("mongodb.port", port);
        }

        @AfterClass
        public static void tearDown() throws Exception {
                if (amqpBroker != null)
                        amqpBroker.stopBroker();
                try {
                        conn.close();
                } catch (Exception e) {
                        //We try to close the connection but if
                        //the connection is closed we just receive the
                        //exception and go on
                }
        }

        protected void createExchange(final String exchangeName, final String queueName) {
                final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
                admin = new RabbitAdmin(ccf);
                queue = new Queue(queueName, false);
                admin.declareQueue(queue);
                final TopicExchange exchange = new TopicExchange(exchangeName);
                admin.declareExchange(exchange);
                admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
                ccf.destroy();
        }

}