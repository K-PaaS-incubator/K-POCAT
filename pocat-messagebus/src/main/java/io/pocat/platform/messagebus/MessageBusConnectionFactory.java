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

import java.util.concurrent.ExecutorService;

/**
 * Convenience factory class to create {@link MessageBusConnection}.
 *
 */
public class MessageBusConnectionFactory {
    /**
     * Context provider to provide {@link EndpointContext} and {@link NamespaceContext}
     */
    private final MessageBusContextProvider context;

    /**
     * Constructor of MessageBus Connection Factory
     * @param context {@link MessageBusContextProvider} to be used by this {@link MessageBusConnection}
     */
    public MessageBusConnectionFactory(MessageBusContextProvider context) {
        this.context = context;
    }

    /**
     * Create new message bus connection. This does not create real connection.
     * @return an instance of MessageBusConnection
     */
    public MessageBusConnection newConnection() {
        return new MessageBusConnectionImpl(this.context);
    }

    /**
     * Create new message bus connection. This does not create real connection.
     * @param executor Executor to be used by this connection
     * @return an instance of MessageBusConnection
     */
    public MessageBusConnection newConnection(ExecutorService executor) {
        return new MessageBusConnectionImpl(this.context, executor);
    }
}
