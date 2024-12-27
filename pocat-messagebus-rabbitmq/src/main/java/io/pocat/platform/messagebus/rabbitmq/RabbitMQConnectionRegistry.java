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

package io.pocat.platform.messagebus.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Connection registry to share connection via publishers or consumer
 * Separate the connections for publishers and consumers to achieve high throughput.
 */
public class RabbitMQConnectionRegistry {
    /**
     * Connection for mapper
     */
    private final Map<ConnectionKey, RabbitMQConnection> connections = new HashMap<>();
    /**
     * Connection creation lock
     */
    private final ReentrantLock createLock = new ReentrantLock();

    /**
     * Find connection for given connection factory and executor
     * @param cf connection factory
     * @param executor executor for connection
     * @return found connection if not exist created one
     */
    public RabbitMQConnection getConnection(RabbitMQConnectionFactory cf, ExecutorService executor) {
        ConnectionKey key = new ConnectionKey(cf, executor);
        if(!connections.containsKey(key)) {
            createLock.lock();
            try {
                if(!connections.containsKey(key)) {
                    connections.put(key, cf.openConnection(executor));
                }
            } finally {
                createLock.unlock();
            }
        }
        return connections.get(key);
    }

    /**
     * Connection find key
     */
    private static class ConnectionKey {
        private final RabbitMQConnectionFactory cf;
        private final ExecutorService executor;

        private ConnectionKey(RabbitMQConnectionFactory cf, ExecutorService executor) {
            this.cf = cf;
            this.executor = executor;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ConnectionKey) {
                ConnectionKey key = (ConnectionKey) obj;
                return cf.equals(key.cf) && executor.equals(key.executor);
            }
            return false;
        }
    }
}
