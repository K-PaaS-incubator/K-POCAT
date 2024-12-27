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

import io.pocat.platform.messagebus.EndpointConnection;
import io.pocat.platform.messagebus.EndpointConsumerGroup;
import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.EndpointPublisher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static io.pocat.platform.messagebus.rabbitmq.RabbitMQConstants.*;

/**
 * Implements of {@link EndpointConnection}
 *
 */
class RabbitMQEndpointConnection implements EndpointConnection {
    /**
     * Endpoint name
     */
    private final String name;
    /**
     * RabbitMQConnectionFactory
     */
    private final RabbitMQConnectionFactory cf = new RabbitMQConnectionFactory();

    /**
     * Constructor
     * Set endpoint properties to connection factory
     * @param descriptor descriptor of endpoint
     * @throws IOException if property value is invalid
     */
    RabbitMQEndpointConnection(EndpointContext descriptor) throws IOException {
        this.name = descriptor.getName();
        try {
            for (String propName : descriptor.getPropertyNames()) {
                switch (propName) {
                    case HOST_PROP_NAME:
                        cf.setHost(descriptor.getProperty(propName));
                        break;
                    case USER_NAME_PROP_NAME:
                        cf.setUsername(descriptor.getProperty(propName));
                        break;
                    case PASSWORD_PROP_NAME:
                        cf.setPassword(descriptor.getProperty(propName));
                        break;
                    case VHOST_PROP_NAME:
                        cf.setVirtualHost(descriptor.getProperty(propName));
                        break;
                    case PORT_PROP_NAME:
                        cf.setPort(Integer.parseInt(descriptor.getProperty(propName)));
                        break;
                    case URI_PROP_NAME:
                        cf.setUri(descriptor.getProperty(propName));
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to create endpoint [" + descriptor.getName() + "] connection.", e);
        }
    }

    /**
     * Return endpoint name
     * @return endpoint name
     */
    @Override
    public String getEndpointName() {
        return name;
    }

    /**
     * Create RabbitMQ endpoint type Publisher
     * @param executor publish executor
     * @return created RabbitMQPublisher
     */
    @Override
    public EndpointPublisher createPublisher(ExecutorService executor) {
        return new RabbitMQPublisher(cf, executor);
    }

    /**
     * Create RabbitMQ endpoint type Consumer group
     * @param groupName consumer group name
     * @param executor  executor to listen message
     * @return created RabbitMQConsumerGroup
     */
    @Override
    public EndpointConsumerGroup createConsumerGroup(String groupName, ExecutorService executor) {
        return new RabbitMQConsumerGroup(cf, groupName, executor);
    }
}
