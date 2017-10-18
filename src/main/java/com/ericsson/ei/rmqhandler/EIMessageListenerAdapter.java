package com.ericsson.ei.rmqhandler;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import com.ericsson.ei.handlers.EventHandler;
import com.rabbitmq.client.Channel;

public class EIMessageListenerAdapter extends MessageListenerAdapter {

    public EIMessageListenerAdapter(Object delegate) {
        super(delegate);
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        Object delegate = getDelegate();
        if (delegate != this) {
            if (delegate instanceof EventHandler) {
                if (channel != null) {
                    channel.basicQos(150);
                    ((EventHandler) delegate).onMessage(message, channel);
                    return;
                }
            }
        }
    }
}
