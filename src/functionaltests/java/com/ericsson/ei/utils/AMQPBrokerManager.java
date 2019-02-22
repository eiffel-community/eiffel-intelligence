package com.ericsson.ei.utils;

import com.google.common.io.Files;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;


public class AMQPBrokerManager {
    public boolean isRunning = false;
    
    private final SystemLauncher systemLauncher = new SystemLauncher();
    
    private String path;
    private String port;

    public AMQPBrokerManager(String path, String port) {
        super();
        this.path = path;
        this.port = port;
    }

    private Map<String, Object> createSystemConfig() {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = AMQPBrokerManager.class.getClassLoader().getResource(path);
        attributes.put("type", "Memory");
        attributes.put("qpid.amqp_port", "" + port);
        attributes.put("qpid.pass_file", "src/test/resources/configs/password.properties");
        attributes.put("qpid.work_dir", Files.createTempDir().getAbsolutePath());
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }
    
    
    public void startBroker() throws Exception {

        try {
            systemLauncher.startup(createSystemConfig());
        } finally {
            systemLauncher.shutdown();
        }
        isRunning = true;
    }

    
    public void stopBroker() {
        systemLauncher.shutdown();
        isRunning = false;
    }
}