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

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper of RabbitMQ {@link com.rabbitmq.client.Connection}
 */
public class RabbitMQConnection {
    /**
     * MAX Connection to rabbitmq broker from this connection
     */
    private static final int DEFAULT_MAX_CONNECTION = Runtime.getRuntime().availableProcessors();

    /**
     * Default max channel per com.rabbitmq.client.Connection
     */
    private static final int DEFAULT_MAX_CHANNEL = Runtime.getRuntime().availableProcessors()*16;

    /**
     * executor for connection
     */
    private final ExecutorService executor;

    /**
     * max channel per com.rabbitmq.client.Connection
     */
    private final int maxChannel;
    /**
     * Connection factory for this connection
     */
    private final ConnectionFactory cf;

    /**
     * Create channel count
     */
    private final AtomicInteger channelCount = new AtomicInteger(0);

    /**
     * Channel pool
     */
    private final BlockingQueue<Channel> channels;

    /**
     * List of connection
     */
    private final List<Connection> connections = new ArrayList<>();

    /**
     * Constructor
     * @param cf Connection factory
     * @param executor executor for this connection
     */
    public RabbitMQConnection(ConnectionFactory cf, ExecutorService executor) {
        this(cf, executor, DEFAULT_MAX_CHANNEL);
    }

    /**
     * Constructor
     * @param cf Connection factory
     * @param executor executor for this connection
     * @param maxChannel max channel per connection
     */
    public RabbitMQConnection(ConnectionFactory cf, ExecutorService executor, int maxChannel) {
        this.cf = cf;
        this.executor = executor;
        this.maxChannel = maxChannel;
        channels = new ArrayBlockingQueue<>(maxChannel);
    }

    /**
     * Get channel from channel pool. if lack of pool, create new one. if channel num is on max, wait pool
     * @return found channel
     * @throws IOException if it encounters a problem to open channel.
     */
    public Channel getChannel() throws IOException {
        Channel channel = channels.poll();
        if(channel == null) {
            channel = createChannel();
        }
        if(channel == null) {
            try {
                channel = channels.take();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        // todo check AutoRecovery of rabbitmq library
        return channel;
    }

    /**
     * Open new channel
     * @return opened channel; null if channel max
     * @throws IOException if it encounters a problem to open channel.
     */
    private synchronized Channel createChannel() throws IOException {
        Channel channel = null;
        if(channelCount.get() < maxChannel) {
            for(Connection connection:connections) {
                // todo check AutoRecovery of rabbitmq library
                // open channel from current connections
                channel = connection.createChannel();
            }
            while(channel == null && connections.size() < DEFAULT_MAX_CONNECTION) {
                // open new connection to create channel. while connection size is not max
                try {
                    Connection connection = cf.newConnection(this.executor);
                    connections.add(0, connection);
                    channel = connection.createChannel();
                } catch (TimeoutException e) {
                    throw new IOException(e);
                }
            }
            if(channel != null) {
                // Channel open success.
                channelCount.incrementAndGet();
                return channel;
            }
        }
        return null;
    }

    /**
     * Return channel to pool
     * @param channel channel to be pooled
     * @throws IOException if it encounters a problem to close channel.
     */
    public void releaseChannel(Channel channel) throws IOException {
        if(!channel.isOpen() || !channels.offer(channel)) {
            try {
                channelCount.decrementAndGet();
                channel.close();
            } catch (TimeoutException e) {
                throw new IOException(e);
            } catch (AlreadyClosedException ignored) {

            }
        }
    }

    /**
     * Close this connection
     * @throws IOException if it encounters a problem to close connection.
     */
    public void close() throws IOException {
        for(Connection connection:connections) {
            connection.close();
        }
    }
}
