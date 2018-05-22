package com.ericsson.ei.flowtests;

import com.google.common.io.Files;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

public class AMQPBrokerManager {

    private final Broker broker = new Broker();
    private String path;
    private int port;

    public AMQPBrokerManager(String path, int port) {
        super();
        this.path = path;
        this.port = port;
    }

    public void startBroker() throws Exception {
        final BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.amqp_port", "" + port);
        brokerOptions.setConfigProperty("qpid.pass_file", "src/test/resources/configs/password.properties");
        brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath());
        brokerOptions.setInitialConfigurationLocation(path);

        broker.startup(brokerOptions);
    }

    public void stopBroker() {
        broker.shutdown();
    }
}