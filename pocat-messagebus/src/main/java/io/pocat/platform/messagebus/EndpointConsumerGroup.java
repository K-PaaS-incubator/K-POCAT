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

/**
 * This interface represents a consumer group of endpoint
 */
public interface EndpointConsumerGroup {
    /**
     * Bind message source to this consumer group
     * At this time, not use real endpoint connection.
     * @param messageSource message source name to bind
     * @throws IOException if it encounters a problem.
     */
    void bind(MessageSource messageSource) throws IOException;

    /**
     * Start subscribing from this group
     *
     * @param handler handler to handle message
     * @throws IOException if it encounters a problem to communicate with endpoint.
     */
    void subscribe(MessageDeliveryHandler handler) throws IOException;

    /**
     * Cancel all subscribing which bound on this consumer group and close connection if needed.
     * @throws IOException if it encounters a problem to communicate with endpoint.
     */
    void close() throws IOException;
}
