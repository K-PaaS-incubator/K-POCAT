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

import java.io.IOException;
import java.util.Map;

/**
 * This interface represents connection of message bus
 */
public interface MessageBusConnection {
    /**
     * Publish message to message bus
     * @param destination message bus destination
     * @param headers message headers
     * @param payload message body
     * @throws IOException if connection is already closed, or it encounters a problem at endpoint publish time.
     */
    void publish(String destination, Map<String, String> headers, byte[] payload) throws IOException;

    /**
     * Bind destination to group. Group will subscribe from destination
     * @param groupName group name to bind destination
     * @param messageSource message source to bind
     * @throws IOException if it encounters a problem at endpoint subscribe time.
     */
    void bind(String groupName, String messageSource) throws IOException;

    /**
     * Subscribe message from message bus with group.
     * Subscribe method must not block caller thread.
     * @param groupName subscribe group name
     * @param handler message handler to handle subscribed message
     * @throws IOException if connection is already closed, consumer group does not exist, or it encounters a problem at endpoint subscribe time.
     */
    void subscribe(String groupName, MessageDeliveryHandler handler) throws IOException;

    /**
     * Close connection
     * @throws IOException if it encounters a problem at endpoint connection close time.
     */
    void close() throws IOException;
}
