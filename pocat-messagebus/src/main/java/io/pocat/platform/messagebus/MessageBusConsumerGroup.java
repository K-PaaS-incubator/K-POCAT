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

package io.pocat.platform.messagebus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Management class to manage all EndpointConsumerGroups with same group name on this message bus connection
 */
class MessageBusConsumerGroup {
    /**
     * Consumer group name
     */
    private final String groupName;
    /**
     * Executor to subscribe
     */
    private final ExecutorService executor;
    /**
     * ConsumerGroup creation lock
     */
    private final ReentrantLock consumerGroupLock = new ReentrantLock();

    /**
     * Managed consumer groups
     */
    private final Map<EndpointConnection, EndpointConsumerGroup> consumerGroups = new HashMap<>();

    /**
     * Constructor
     * @param groupName consumer group name
     * @param executor executor to be used by consumer groups
     */
    MessageBusConsumerGroup(String groupName, ExecutorService executor) {
        this.groupName = groupName;
        this.executor = executor;
    }

    /**
     * Return consumer group name
     * @return consumer group name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Start subscribe from all consumer group
     * @param handler subscribed message handler
     * @throws IOException if it encounters a problem to communicate with endpoint.
     */
    public void subscribe(MessageDeliveryHandler handler) throws IOException {
        for (EndpointConsumerGroup consumerGroup : this.consumerGroups.values()) {
            consumerGroup.subscribe(handler);
        }
    }

    /**
     * Bind message source to consumer group
     * Create consumer group if not exist.
     *
     * @param msgSrc message source to bind
     * @throws IOException if it encounters a problem to bind message source.
     */
    public void bind(MessageSource msgSrc) throws IOException {
        if (!this.consumerGroups.containsKey(msgSrc.getNamespace().getEndpointConnection())) {
            this.consumerGroupLock.lock();
            try {
                if (!this.consumerGroups.containsKey(msgSrc.getNamespace().getEndpointConnection())) {
                    this.consumerGroups.put(msgSrc.getNamespace().getEndpointConnection(), createConsumerGroup(this.groupName, msgSrc.getNamespace()));
                }
            } finally {
                this.consumerGroupLock.unlock();
            }
        }
        EndpointConsumerGroup consumerGroup = this.consumerGroups.get(msgSrc.getNamespace().getEndpointConnection());
        consumerGroup.bind(msgSrc);
    }

    /**
     * Close all message bus consumer group with same name
     * @throws IOException if it encounters a problem to close. only last exception is thrown
     */
    public void close() throws IOException {
        IOException last = null;
        for (EndpointConsumerGroup consumerGroup : this.consumerGroups.values()) {
            try {
                consumerGroup.close();
            } catch (IOException e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
    }

    /**
     * Create consumer group
     * @param groupName consumer group name
     * @param namespace namespace of consumer group
     * @return created consumer group
     */
    private EndpointConsumerGroup createConsumerGroup(String groupName, Namespace namespace) {
        return namespace.getEndpointConnection().createConsumerGroup(groupName, this.executor);
    }
}
