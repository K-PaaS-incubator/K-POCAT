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
import io.pocat.platform.messagebus.MessageDeliveryHandler;

import java.io.IOException;

public class ConsumerGroup {
    private final MessageBusConnection connection;
    private final String consumerGroupName;
    private final MessageDeliveryHandler deliveryHandler;

    public ConsumerGroup(MessageBusConnection connection, String consumerGroupName, MessageDeliveryHandler deliveryHandler) {
        this.connection = connection;
        this.consumerGroupName = consumerGroupName;
        this.deliveryHandler = deliveryHandler;
    }

    public void start() throws IOException {
        connection.subscribe(consumerGroupName, deliveryHandler);
    }

    public void stop() {

    }
}
