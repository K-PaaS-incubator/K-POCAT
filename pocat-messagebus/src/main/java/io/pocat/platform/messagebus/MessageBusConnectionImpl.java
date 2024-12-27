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
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link MessageBusConnection}
 */
class MessageBusConnectionImpl implements MessageBusConnection {
    /**
     * Connection name space separator
     */
    private static final char NAMESPACE_SEPARATOR = ':';

    /**
     * Context provider to provide {@link EndpointContext} and {@link NamespaceContext}
     */
    private final MessageBusContextProvider context;

    /**
     * Executor for this connection.
     */
    private final ExecutorService executor;

    /**
     * Endpoint Connection Provider. This will provide real endpoint connection which is wrapped by {@link EndpointConnection}.
     */
    private final EndpointConnectionProvider endpointConnectionProvider = new EndpointConnectionProvider();

    /**
     * NameSpace object and name mapper
     */
    private final Map<String, Namespace> namespaces = new HashMap<>();

    /**
     * EndpointConnection object and endpoint name mapper
     */
    private final Map<String, EndpointConnection> endpointConnections = new HashMap<>();

    /**
     * Consumer Group object and group name mapper
     */
    private final Map<String, MessageBusConsumerGroup> consumerGroups = new HashMap<>();

    /**
     * Publisher to publish message to message bus
     */
    private final MessageBusPublisher publisher;

    private final ReentrantLock nameSpaceLock = new ReentrantLock();
    private final ReentrantLock consumerGroupLock = new ReentrantLock();
    private final ReentrantLock endpointsLock = new ReentrantLock();

    /**
     * If true, Executor is controlled inside connection.
     */
    private boolean isOwnPool = false;

    /**
     * If true, this connection is already closed, and cannot do any action.
     */
    private boolean isClosed = false;

    /**
     * Constructor of MessageBusConnectionImpl
     *
     * @param context Context holder to get {@link EndpointContext} and {@link NamespaceContext}
     */
    MessageBusConnectionImpl(MessageBusContextProvider context) {
        // Use work stealing pool
        this(context, Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors()));
        // Check pool is controlled by this connection.
        this.isOwnPool = true;
    }

    /**
     * Constructor of MessageBusConnectionImpl
     *
     * @param context  Context holder to get {@link EndpointContext} and {@link NamespaceContext}
     * @param executor Executor to be used by this connection
     */
    public MessageBusConnectionImpl(MessageBusContextProvider context, ExecutorService executor) {
        this.context = context;
        this.executor = executor;
        this.publisher = new MessageBusPublisher(executor);
    }

    /**
     * Publish message.
     *
     * @param destination destination name to publish
     * @param headers     message header
     * @param payload     message body
     * @throws IOException if connection is already closed, or it encounters a problem at endpoint publish time.
     */
    @Override
    public void publish(String destination, Map<String, String> headers, byte[] payload) throws IOException {
        checkClosed();

        Destination dest = buildDestination(destination);
        this.publisher.publish(dest, headers, payload);
    }

    /**
     * Bind message source to consumer group. consumer group will subscribe message from this source
     *
     * @param groupName     Consumer group name
     * @param messageSource message source to bind
     * @throws IOException if connection is already closed, or it encounters a problem.
     */
    @Override
    public void bind(String groupName, String messageSource) throws IOException {
        checkClosed();

        MessageSource msgSrc = buildMessageSource(messageSource);
        if (!this.consumerGroups.containsKey(groupName)) {
            consumerGroupLock.lock();
            try {
                if (!this.consumerGroups.containsKey(groupName)) {
                    this.consumerGroups.put(groupName, new MessageBusConsumerGroup(groupName, this.executor));
                }
            } finally {
                consumerGroupLock.unlock();
            }
        }
        MessageBusConsumerGroup group = this.consumerGroups.get(groupName);
        group.bind(msgSrc);
    }

    /**
     * Start subscribing from ConsumerGroup
     *
     * @param groupName consumer group name
     * @param handler   consumer group message handler
     * @throws IOException if connection is already closed, consumer group does not exist, or it encounters a problem at endpoint subscribe time.
     */
    @Override
    public void subscribe(String groupName, MessageDeliveryHandler handler) throws IOException {
        checkClosed();

        if (!this.consumerGroups.containsKey(groupName)) {
            throw new IOException("Consumer group [" + groupName + "] does not exist.");
        }

        this.consumerGroups.get(groupName).subscribe(handler);
    }

    /**
     * Close connection
     *
     * @throws IOException if it encounters a problem at endpoint close time.
     */
    @Override
    public void close() throws IOException {
        checkClosed();

        for (MessageBusConsumerGroup group : this.consumerGroups.values()) {
            group.close();
        }
        this.publisher.close();

        if (this.isOwnPool) {
            this.executor.shutdown();
        }

        this.isClosed = true;
    }

    /**
     * Check this connection is already closed
     *
     * @throws IOException if this connection is already closed
     */
    private void checkClosed() throws IOException {
        if (this.isClosed) {
            throw new IOException("Already closed.");
        }
    }

    /**
     * Parse destination name and create Destination Object
     *
     * @param destination destination name to create
     * @return destination instance
     * @throws IllegalArgumentException if destination name is invalid
     * @throws IOException              problem to find namespace
     */
    private Destination buildDestination(String destination) throws IOException {
        int index = destination.indexOf(NAMESPACE_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid destination [" + destination + "].");
        }
        String nameSpaceName = destination.substring(0, index);
        String topic = destination.substring(index + 1);

        if (nameSpaceName.isEmpty() || topic.isEmpty()) {
            throw new IllegalArgumentException("Invalid destination [" + destination + "].");
        }
        findNamespace(nameSpaceName);
        return new DestinationImpl(this, this.namespaces.get(nameSpaceName), topic);
    }

    /**
     * Parse message source name and create MessageSource Object
     *
     * @param messageSource message source name to create
     * @return MessageSource instance
     * @throws IllegalArgumentException if message source name is invalid
     * @throws IOException              problem to find namespace
     */
    private MessageSource buildMessageSource(String messageSource) throws IOException {
        int index = messageSource.indexOf(NAMESPACE_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid message source [" + messageSource + "].");
        }
        String nameSpaceName = messageSource.substring(0, index);
        String topic = messageSource.substring(index + 1);

        if (nameSpaceName.isEmpty() || topic.isEmpty()) {
            throw new IllegalArgumentException("Invalid message source [" + messageSource + "].");
        }
        return new MessageSourceImpl(this, findNamespace(nameSpaceName), topic);
    }

    /**
     * Get namespace. if not exist namespace, create new one.
     *
     * @param nameSpaceName namespace name to find
     * @return found namespace
     * @throws IOException if failed to create namespace
     */
    private Namespace findNamespace(String nameSpaceName) throws IOException {
        if (!this.namespaces.containsKey(nameSpaceName)) {
            this.nameSpaceLock.lock();
            try {
                if (!this.namespaces.containsKey(nameSpaceName)) {
                    this.namespaces.put(nameSpaceName, createNamespace(nameSpaceName));
                }
            } finally {
                this.nameSpaceLock.unlock();
            }
        }
        return this.namespaces.get(nameSpaceName);
    }

    /**
     * @param namespaceName namespace name to create
     * @return NameSpace instance
     * @throws IllegalArgumentException if invalid namespace configuration
     * @throws IOException              problem to find endpoint
     */
    private Namespace createNamespace(String namespaceName) throws IOException {
        NamespaceContext context = this.context.getNamespaceContext(namespaceName);
        if(context == null) {
            throw new IllegalArgumentException("Namespace [" + namespaceName + "] does not exist");
        }
        EndpointConnection endpointConnection;
        if (context.getEndpointRef() != null) {
            if (!endpointConnections.containsKey(context.getEndpointRef())) {
                endpointsLock.lock();
                try {
                    if (!endpointConnections.containsKey(context.getEndpointRef())) {
                        EndpointConnection epCon = endpointConnectionProvider.provideConnection(this.context.getEndpointContext(context.getEndpointRef()));
                        endpointConnections.put(context.getEndpointRef(), epCon);
                    }
                } finally {
                    endpointsLock.unlock();
                }
            }
            endpointConnection = endpointConnections.get(context.getEndpointRef());
        } else if (context.getEndpointContext() != null) {
            // endpoint cannot be referenced.
            endpointConnection = endpointConnectionProvider.provideConnection(context.getEndpointContext());
        } else {
            throw new IllegalArgumentException("Endpoint config does not exist.");
        }
        return new NamespaceImpl(namespaceName, endpointConnection, context);
    }
}
