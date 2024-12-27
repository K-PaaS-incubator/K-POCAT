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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.pocat.platform.messagebus.EndpointPublisher;
import io.pocat.platform.messagebus.Destination;
import io.pocat.platform.messagebus.Namespace;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Implements of {@link EndpointPublisher}
 */
public class RabbitMQPublisher implements EndpointPublisher {
    /**
     * RabbitMQ Connection registry to share connection via publishers
     */
    private static final RabbitMQConnectionRegistry CONNECTION_REGISTRY = new RabbitMQConnectionRegistry();

    /**
     * RabbitMQ Connection factory for this publisher
     */
    private final RabbitMQConnectionFactory cf;

    /**
     * Executor for this publisher
     */
    private final ExecutorService executor;

    /**
     * Constructor
     * @param cf rabbitmq connection factory for this publisher
     * @param executor executor for this publisher
     */
    public RabbitMQPublisher(RabbitMQConnectionFactory cf, ExecutorService executor) {
        this.cf = cf;
        this.executor = executor;
    }

    /**
     * Publish message to destination
     * @param destination destination to publish
     * @param headers published message headers
     * @param payload published message payload
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    @Override
    public void publish(Destination destination, Map<String, String> headers, byte[] payload) throws IOException {
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        Channel channel = connection.getChannel();
        try {
            String exchangeName = checkExchange(destination.getNamespace());
            channel.basicPublish(exchangeName, destination.getTopic(), headersToProp(headers), payload);
        } finally {
            if(channel != null) {
                connection.releaseChannel(channel);
            }
        }
    }

    /**
     * Close this publisher
     */
    @Override
    public void close() {
        // do nothing
    }

    /**
     * Test exchange existence. if not exist and declare if absent is true, try to create exchange
     *
     * @param namespace namespace of this exchange
     * @return exchange name
     * @throws IOException if declare failed or exchange does not exist.
     */
    private String checkExchange(Namespace namespace) throws IOException {
        String exchangeName = namespace.getProperty(RabbitMQConstants.EXCHANGE_NAME_PROP_NAME);
        if(exchangeName == null) {
            // Default Exchange always exist and cannot be removed. So, no need to process below
            return RabbitMQConstants.DEFAULT_EXCHANGE_NAME;
        }
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        Channel channel = connection.getChannel();
        try {
            if(Boolean.parseBoolean(namespace.getProperty(RabbitMQConstants.EXCHANGE_DECLARE_IF_ABSENT_PROP_NAME, "true"))) {
                String type = namespace.getProperty(RabbitMQConstants.EXCHANGE_TYPE_PROP_NAME, RabbitMQConstants.DEFAULT_EXCHANGE_TYPE);
                channel.exchangeDeclare(exchangeName, type);
            } else {
                channel.exchangeDeclarePassive(exchangeName);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } finally {
            if(channel != null) {
                connection.releaseChannel(channel);
            }
        }
        return exchangeName;
    }

    /**
     * Convert message headers to rabbitmq amqp basic properties
     * @param headers headers to convert
     * @return converted BasicProperties; null if headers is null or 0 sized
     */
    private AMQP.BasicProperties headersToProp(Map<String, String> headers) {
        if(headers == null||headers.size() == 0) {
            return null;
        }

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        builder.headers(Collections.unmodifiableMap(headers));

        return builder.build();
    }
}
