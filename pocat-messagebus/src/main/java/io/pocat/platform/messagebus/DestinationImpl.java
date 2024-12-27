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

/**
 * Implements of {@link Destination}
 */
class DestinationImpl implements Destination {
    private final MessageBusConnection connection;
    private final Namespace namespace;
    private final String topic;
    private final String name;

    public DestinationImpl(MessageBusConnection connection, Namespace namespace, String topic) {
        this.connection = connection;
        this.namespace = namespace;
        this.topic = topic;
        this.name = namespace.getName() + ":" + topic;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public MessageBusConnection getConnection() {
        return connection;
    }
}
