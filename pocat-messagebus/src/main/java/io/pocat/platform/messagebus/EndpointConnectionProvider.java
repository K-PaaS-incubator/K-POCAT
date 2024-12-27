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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * EndpointConnectionFactory service loader
 * Find EndpointConnectionFactory with Service Provider Interface(SPI)
 */
class EndpointConnectionProvider {
    /**
     * EndpointConnectionFactories found in SPI
     */
    private List<EndpointConnectionFactory> factories;

    /**
     * Constructor
     */
    public EndpointConnectionProvider() {
        reload();
    }

    /**
     * Create endpointConnection from found factory
     *
     * @param descriptor endpoint context to create endpoint connection
     * @return endpoint connection
     * @throws IOException if failed to connect endpoint
     * @throws IllegalArgumentException if suitable factory not found
     */
    public EndpointConnection provideConnection(EndpointContext descriptor) throws IOException {
        for(EndpointConnectionFactory factory:factories) {
            if(factory.isSupportedEndpointType(descriptor.getEndpointType())) {
                return factory.createConnection(descriptor);
            }
        }
        throw new IllegalArgumentException("Not supported endpoint type [" + descriptor.getEndpointType() + "].");
    }

    /**
     * Refresh SPI list
     */
    private void reload() {
        this.factories = new ArrayList<>();
        ServiceLoader<EndpointConnectionFactory> factories = ServiceLoader.load(EndpointConnectionFactory.class);
        for(EndpointConnectionFactory factory:factories) {
            this.factories.add(factory);
        }
    }
}
