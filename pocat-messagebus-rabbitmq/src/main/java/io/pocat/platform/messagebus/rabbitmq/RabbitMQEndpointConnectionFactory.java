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
import io.pocat.platform.messagebus.EndpointConnectionFactory;
import io.pocat.platform.messagebus.EndpointContext;

import java.io.IOException;

/**
 * Service Provider Interface of {@link EndpointConnectionFactory}
 */
public class RabbitMQEndpointConnectionFactory implements EndpointConnectionFactory {
    /**
     * Supported Endpoint type : rabbitmq
     */
    private static final String RABBIT_MQ_TYPE = "rabbitmq";
    /**
     * Supported Endpoint type : amqp
     */
    private static final String AMQP_TYPE = "amqp";

    /**
     * Create rabbitmq type endpoint connection
     * @param descriptor descriptor of endpoint
     * @return rabbitmq endpoint connection
     * @throws IOException if it encounters a problem to create endpoint connection
     */
    @Override
    public EndpointConnection createConnection(EndpointContext descriptor) throws IOException {
        return new RabbitMQEndpointConnection(descriptor);
    }

    /**
     * Test endpoint type is rabbitmq or amqp
     * @param endpointType type of endpoint
     * @return true if endpoint type is "rabbitmq" or "amqp"; false otherwise.
     */
    @Override
    public boolean isSupportedEndpointType(String endpointType) {
        return RABBIT_MQ_TYPE.equalsIgnoreCase(endpointType) || AMQP_TYPE.equalsIgnoreCase(endpointType);
    }
}
