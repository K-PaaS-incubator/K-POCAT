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
 * This interface represents a publisher to endpoint
 */
public interface EndpointPublisher {
    /**
     * Publish message to this endpoint with destination
     * @param destination destination to publish
     * @param headers published message headers
     * @param payload published message payload
     * @throws IOException if it encounters a problem to communicate with endpoint.
     */
    void publish(Destination destination, Map<String, String> headers, byte[] payload) throws IOException;

    /**
     * Close this publisher.
     * @throws IOException if it encounters a problem to communicate with endpoint.
     */
    void close() throws IOException;
}
