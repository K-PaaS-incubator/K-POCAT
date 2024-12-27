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

import java.util.concurrent.ExecutorService;

/**
 * This interface represents connection to endpoint
 */
public interface EndpointConnection {
    /**
     * Get endpoint name
     * @return endpoint name
     */
    String getEndpointName();

    /**
     * Create publisher to this endpoint
     * always create new publisher
     * @param executor publish executor
     * @return created publisher
     */
    EndpointPublisher createPublisher(ExecutorService executor);

    /**
     * Create consumer group connected to this endpoint
     * Always create new group with same name
     * @param groupName consumer group name
     * @param executor executor to listen message
     * @return created endpoint consumer group
     */
    EndpointConsumerGroup createConsumerGroup(String groupName, ExecutorService executor);
}
