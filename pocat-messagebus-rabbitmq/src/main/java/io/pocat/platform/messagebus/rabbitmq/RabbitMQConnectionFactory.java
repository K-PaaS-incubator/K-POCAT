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

import com.rabbitmq.client.ConnectionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;

/**
 * Connection Factory wrapper for {@link com.rabbitmq.client.ConnectionFactory}
 */
public class RabbitMQConnectionFactory {
    private final ConnectionFactory cf;

    public RabbitMQConnectionFactory() {
        cf = new ConnectionFactory();
    }

    public void setHost(String host) {
        cf.setHost(host);
    }

    public void setPort(int port) {
        cf.setPort(port);
    }

    public void setUsername(String username) {
        cf.setUsername(username);
    }

    public void setPassword(String password) {
        cf.setPassword(password);
    }

    public void setVirtualHost(String virtualHost) {
        cf.setVirtualHost(virtualHost);
    }

    public void setUri(URI uri) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        cf.setUri(uri);
    }

    public void setUri(String uriString) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        setUri(new URI(uriString));
    }

    public RabbitMQConnection openConnection(ExecutorService executor) {
        return new RabbitMQConnection(cf, executor);
    }
}
