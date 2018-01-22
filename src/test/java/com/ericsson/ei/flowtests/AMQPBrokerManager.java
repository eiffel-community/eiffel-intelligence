package com.ericsson.ei.flowtests;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

public class AMQPBrokerManager {
    private static final String PORT = "8672";
    private final Broker broker = new Broker();
    private String path;

    public AMQPBrokerManager(String path) {
        super();
        this.path = path;
    }

    public void startBroker() throws Exception {
        final BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.amqp_port", PORT);
        brokerOptions.setConfigProperty("qpid.pass_file", "src/test/resources/configs/password.properties");
        brokerOptions.setInitialConfigurationLocation(path);

        broker.startup(brokerOptions);
    }

    public void stopBroker() {
        broker.shutdown();
    }
}