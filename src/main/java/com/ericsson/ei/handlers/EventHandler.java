package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.rabbitmq.client.Channel;

@Component
public class EventHandler implements ChannelAwareMessageListener {

    private static Logger log = LoggerFactory.getLogger(EventHandler.class);

    @Autowired
    RulesHandler rulesHandler;

    @Autowired
    IdRulesHandler idRulesHandler;

    public void eventReceived(String event) {
        RulesObject eventRules = rulesHandler.getRulesForEvent(event);
        idRulesHandler.runIdRules(eventRules, event);
    }

    public void eventReceived(byte[] message) {
        String actualMessage = new String(message);
        log.info("Event received <" + actualMessage + ">");
        eventReceived(actualMessage);
        if (System.getProperty("flow.test") == "true") {
            String countStr = System.getProperty("eiffel.intelligence.processedEventsCount");
            int count = Integer.parseInt(countStr);
            count++;
            System.setProperty("eiffel.intelligence.processedEventsCount", "" + count);
        }
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        byte[] messageBody = message.getBody();
        eventReceived(messageBody);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, false);
        int breakHere = 1;

    }
}
