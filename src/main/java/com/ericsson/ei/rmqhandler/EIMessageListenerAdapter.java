/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
