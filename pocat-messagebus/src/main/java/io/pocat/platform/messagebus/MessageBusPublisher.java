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
 * Management class to manage all EndpointPublisher on this message bus connection
 */
class MessageBusPublisher {
    /**
     * Managed publishers
     */
    private final Map<EndpointConnection, EndpointPublisher> publishers = new HashMap<>();
    /**
     * Executor to process publish
     */
    private final ExecutorService executor;

    /**
     * Publisher creation lock
     */
    private final ReentrantLock publisherLock = new ReentrantLock();

    /**
     * Constructor
     * @param executor executor to process publish
     */
    MessageBusPublisher(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Publish message to destination
     * @param destination destination to publish message
     * @param headers message headers
     * @param payload message payload
     * @throws IOException if it encounters a problem to communicate with endpoint
     */
    public void publish(Destination destination, Map<String, String> headers, byte[] payload) throws IOException {
        // if publisher not exist. create
        if (!publishers.containsKey(destination.getNamespace().getEndpointConnection())) {
            publisherLock.lock();
            try {
                // double check. if prev thread create same publisher
                if (!publishers.containsKey(destination.getNamespace().getEndpointConnection())) {
                    publishers.put(destination.getNamespace().getEndpointConnection(), createPublisher(destination.getNamespace()));
                }
            } finally {
                publisherLock.unlock();
            }

        }
        EndpointPublisher publisher = publishers.get(destination.getNamespace().getEndpointConnection());
        publisher.publish(destination, headers, payload);
    }

    /**
     * Create publisher
     * @param namespace namespace to publish
     * @return created publisher
     */
    private EndpointPublisher createPublisher(Namespace namespace) {
        return namespace.getEndpointConnection().createPublisher(executor);
    }

    /**
     * Close all publisher
     * @throws IOException if it encounters a problem to close. only last exception is thrown
     */
    public void close() throws IOException {
        IOException last = null;
        for (EndpointPublisher publisher : publishers.values()) {
            try {
                publisher.close();
            } catch (IOException e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
    }
}
