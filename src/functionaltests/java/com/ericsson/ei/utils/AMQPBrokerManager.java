package com.ericsson.ei.utils;

import com.google.common.io.Files;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

public class AMQPBrokerManager {
    public boolean isRunning = false;

    private final Broker broker = new Broker();
    private String path;
    private String port;

    public AMQPBrokerManager(String path, String port) {
        super();
        this.path = path;
        this.port = port;
    }

    public void startBroker() throws Exception {
        final BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.amqp_port", port);
        brokerOptions.setConfigProperty("qpid.pass_file", "src/functionaltests/resources/configs/password.properties");
        brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath());
        brokerOptions.setInitialConfigurationLocation(path);

        broker.startup(brokerOptions);
        isRunning = true;
    }

    public void stopBroker() {
        broker.shutdown();
        isRunning = false;
    }
}