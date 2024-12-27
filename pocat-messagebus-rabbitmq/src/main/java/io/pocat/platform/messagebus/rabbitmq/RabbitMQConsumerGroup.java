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
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.pocat.platform.messagebus.EndpointConsumerGroup;
import io.pocat.platform.messagebus.MessageDeliveryHandler;
import io.pocat.platform.messagebus.MessageSource;
import io.pocat.platform.messagebus.Namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Implements of {@link EndpointConsumerGroup}
 */
public class RabbitMQConsumerGroup implements EndpointConsumerGroup {
    /**
     * RabbitMQ Connection registry to share connection via consumer groups
     */
    private static final RabbitMQConnectionRegistry CONNECTION_REGISTRY = new RabbitMQConnectionRegistry();

    /**
     * Rabbit MQ Connection Factory
     */
    private final RabbitMQConnectionFactory cf;
    /**
     * Queue name is consumer group name. RabbitMQ consumer group is treated rabbitmq queue.
     */
    private final String queueName;
    /**
     * Executor for this consumer group
     */
    private final ExecutorService executor;

    /**
     * exchange and namespace mapper
     * namespace is treated exchange in rabbitmq message bus. but not a same name. so mapper needs
     */
    private final Map<String, String> exchangeNamespaceMapper = new HashMap<>();
    /**
     * list of message source to bind
     */
    private final List<MessageSource> messageSources = new ArrayList<>();
    /**
     * this queue consumer tag
     */
    private String consumerTag;
    private Channel channel;

    /**
     * Constructor
     * @param cf connection factory of rabbitmq
     * @param queueName consumer group name
     * @param executor executor for this consumer group
     */
    public RabbitMQConsumerGroup(RabbitMQConnectionFactory cf, String queueName, ExecutorService executor) {
        this.cf = cf;
        this.queueName = queueName;
        this.executor = executor;
    }

    /**
     * Bind message source to this consumer group
     * @param messageSource message source name to bind
     */
    @Override
    public void bind(MessageSource messageSource) {
        messageSources.add(messageSource);
    }

    /**
     * Start subscribe consumer group
     * @param handler handler to handle message
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    @Override
    public void subscribe(MessageDeliveryHandler handler) throws IOException {
        checkQueue();
        bindSources();
        startConsume(handler);
    }

    /**
     * Cancel queue consume with consumer tag
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    @Override
    public void close() throws IOException {
        if(consumerTag != null) {
            RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);

            this.channel.basicCancel(this.consumerTag);
            connection.releaseChannel(channel);
            this.channel = null;
            this.consumerTag = null;
        }
    }

    /**
     * Test queue existence and create queue if not exist
     * @throws IOException  if it encounters a problem to communicate with rabbitmq broker.
     */
    private void checkQueue() throws IOException {
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        Channel channel = connection.getChannel();

        try {
            channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            connection.releaseChannel(channel);
            channel = connection.getChannel();
            channel.queueDeclare(queueName, true, false, true, null);
        } finally {
            if(channel != null) {
                connection.releaseChannel(channel);
            }
        }

    }

    /**
     * Bind exchange and routing key to queue
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    private void bindSources() throws IOException {
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        for (MessageSource messageSource : messageSources) {
            String namespace = messageSource.getNamespace().getName();
            String exchangeName = checkExchange(messageSource.getNamespace());

            Channel channel = connection.getChannel();
            try {
                exchangeNamespaceMapper.put(exchangeName, namespace);
                channel.queueBind(queueName, exchangeName, messageSource.getTopic());
            } finally {
                if(channel != null) {
                    connection.releaseChannel(channel);
                }
            }
        }
    }

    /**
     * Start consume queue
     * @param handler handler to handle rabbitmq message
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    private void startConsume(MessageDeliveryHandler handler) throws IOException {
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        Channel channel = connection.getChannel();
        try {
            this.consumerTag = channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    executor.execute(() -> {
                        // convert rabbitmq message to MessageBus Delivery
                        Map<String, String> headers = propertiesToHeaders(properties);
                        String namespaceName = exchangeNamespaceMapper.get(envelope.getExchange());
                        if(namespaceName == null) {
                            // use exchange name as namespace if matched namespace not found
                            namespaceName = envelope.getExchange();
                        }
                        String msgSrcName = namespaceName + ":" + envelope.getRoutingKey();
                        handler.onDelivery(msgSrcName, headers, body);
                    });
                }
            });
            // hold channel to cancel consumer
            this.channel = channel;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    /**
     * Test exchange existence. if not exist and declare if absent is true, try to create exchange
     * @param namespace namespace of this exchange
     * @return exchange name
     * @throws IOException if it encounters a problem to communicate with rabbitmq broker.
     */
    private String checkExchange(Namespace namespace) throws IOException {
        String exchangeName = namespace.getProperty(RabbitMQConstants.EXCHANGE_NAME_PROP_NAME, RabbitMQConstants.DEFAULT_EXCHANGE_NAME);
        RabbitMQConnection connection = CONNECTION_REGISTRY.getConnection(cf, executor);
        Channel channel = null;
        try {
            channel = connection.getChannel();

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
     * Convert rabbitmq amqp basic properties to message headers
     * @param properties properties to convert
     * @return converted message header map
     */
    private Map<String, String> propertiesToHeaders(AMQP.BasicProperties properties) {
        Map<String, String> headers = new HashMap<>();
        if(properties != null && properties.getHeaders() != null) {
            for (Map.Entry<String, Object> headerEntry : properties.getHeaders().entrySet()) {
                headers.put(headerEntry.getKey(), headerEntry.getValue().toString());
            }
        }
        return headers;
    }
}
