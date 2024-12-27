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

import io.pocat.platform.messagebus.EndpointConnection;
import io.pocat.platform.messagebus.EndpointConsumerGroup;
import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.EndpointPublisher;

import java.util.concurrent.ExecutorService;

public class MockEndpointConnection implements EndpointConnection {
    private final EndpointContext descriptor;

    public MockEndpointConnection(EndpointContext descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getEndpointName() {
        return descriptor.getName();
    }

    @Override
    public EndpointPublisher createPublisher(ExecutorService executor) {
        return new MockEndpointPublisher(executor);
    }

    @Override
    public EndpointConsumerGroup createConsumerGroup(String groupName, ExecutorService executor) {
        return new MockEndpointConsumerGroup();
    }
}
