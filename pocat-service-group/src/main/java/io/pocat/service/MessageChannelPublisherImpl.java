/*
 * Copyright 2024. dongobi soft inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pocat.service;

import io.pocat.platform.messagebus.MessageBusConnection;

import java.io.IOException;
import java.util.Map;

public class MessageChannelPublisherImpl implements MessageChannelPublisher {
    private String destination;
    private MessageBusConnection conn;

    public MessageChannelPublisherImpl(String destination, MessageBusConnection conn) {
        this.destination = destination;
        this.conn = conn;
    }

    @Override
    public void publish(Map<String, String> headers, byte[] payload) throws IOException {
        conn.publish(destination, headers, payload);
        // todo AccessLogger.getInstance().log();
    }
}
