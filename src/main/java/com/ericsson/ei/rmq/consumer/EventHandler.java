package com.ericsson.ei.rmq.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

    private static Logger log = LoggerFactory.getLogger(EventHandler.class);

    public void eventReceived(byte[] message) {
        String actualMessage = new String(message);
        log.info("Event received <" + actualMessage + ">");
    }
}