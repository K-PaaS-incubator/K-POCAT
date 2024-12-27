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

import com.rabbitmq.client.BuiltinExchangeType;

/**
 * Collection of RabbitMQ constants
 */
public class RabbitMQConstants {
    /**
     * Default exchange name of rabbitmq
     */
    public static final String DEFAULT_EXCHANGE_NAME = "amq.topic";

    /**
     * default exchange type
     */
    public static final String DEFAULT_EXCHANGE_TYPE = BuiltinExchangeType.TOPIC.getType();

    /**
     * rabbitmq exchange name property name in namespace context
     */
    public static final String EXCHANGE_NAME_PROP_NAME = "rabbitmq.exchange";

    /**
     * if rabbitmq exchange does not exist, allows declare. in namespace context
     */
    public static final String EXCHANGE_DECLARE_IF_ABSENT_PROP_NAME = "rabbitmq.exchange.declare";

    /**
     * rabbitmq exchange type in namespace context
     */
    public static final String EXCHANGE_TYPE_PROP_NAME = "rabbitmq.exchange.type";

    /**
     * host property name in rabbitmq endpoint context
     */
    public static final String HOST_PROP_NAME = "rabbitmq.host";

    /**
     * username property name in rabbitmq endpoint context
     */
    public static final String USER_NAME_PROP_NAME = "rabbitmq.username";

    /**
     * password property name in rabbitmq endpoint context
     */
    public static final String PASSWORD_PROP_NAME = "rabbitmq.password";

    /**
     * vhost property name in rabbitmq endpoint context
     */
    public static final String VHOST_PROP_NAME = "rabbitmq.vhost";

    /**
     * port property name in rabbitmq endpoint context
     */
    public static final String PORT_PROP_NAME = "rabbitmq.port";

    /**
     * uri property name in rabbitmq endpoint context
     */
    public static final String URI_PROP_NAME = "rabbitmq.uri";
}
