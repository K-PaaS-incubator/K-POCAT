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

package io.pocat.messagebus.test;

import io.pocat.platform.messagebus.EndpointConsumerGroup;
import io.pocat.platform.messagebus.MessageDeliveryHandler;
import io.pocat.platform.messagebus.MessageSource;

import java.util.ArrayList;
import java.util.List;

public class MockEndpointConsumerGroup implements EndpointConsumerGroup {
    private final List<String> channels = new ArrayList<>();

    public MockEndpointConsumerGroup() {
    }

    @Override
    public void bind(MessageSource messageSource) {
        channels.add(messageSource.getName());
    }

    @Override
    public void subscribe(MessageDeliveryHandler handler) {
        for(String channel:channels) {
            MockChannel mockChannel = MockChannelManager.getInstance().getChannel(channel);
            mockChannel.subscribe(handler);
        }
    }

    @Override
    public void close() {

    }
}
