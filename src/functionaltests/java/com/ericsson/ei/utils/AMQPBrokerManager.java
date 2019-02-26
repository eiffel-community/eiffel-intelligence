package com.ericsson.ei.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.apache.qpid.server.model.SystemConfig;
import org.apache.qpid.server.store.MemoryMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQPBrokerManager {
    private String path;
    private int port;
    public boolean isRunning = false;
    private String passwordFile = "src/functionaltests/resources/configs/password.properties";

    private SystemLauncher systemLauncher = new SystemLauncher();

    private final static Logger LOGGER = LoggerFactory.getLogger(TestConfigs.class);

    public AMQPBrokerManager(String path, int port) {
        super();
        this.path = path;
        this.port = port;
    }

    private Map<String, Object> createSystemConfig() {
        String passwordPath = new File(passwordFile).getAbsolutePath();
        Map<String, String> context = new HashMap<>();
        context.put("qpid.amqp_port", "" + port);
        context.put("qpid.pass_file", passwordPath);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SystemConfig.CONTEXT, context);
        attributes.put(SystemConfig.TYPE, MemoryMessageStore.TYPE);
        attributes.put(SystemConfig.INITIAL_CONFIGURATION_LOCATION, path);
        return attributes;
    }

    public void startBroker() throws Exception {
        try {
            systemLauncher.startup(createSystemConfig());
        } catch (Exception e) {
            LOGGER.error("Create QPID System Config fail. \nError: {}", e.getMessage());
            e.printStackTrace();
        }
        isRunning = true;
    }

    public void stopBroker() {
        systemLauncher.shutdown();
        isRunning = false;
    }
}