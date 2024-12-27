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

package io.pocat.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Find {@link ResourceFactory} with Service Provider Interface(SPI)
 */
class ResourceFactoryProvider {
    /**
     * List of provided resource factory
     */
    private List<ResourceFactory> resourceFactories;

    /**
     * Constructor
     */
    public ResourceFactoryProvider() {
        reload();
    }

    /**
     * Clear this current list and reload all providers.
     */
    public void reload() {
        // clear current provided resources
        this.resourceFactories = new ArrayList<>();
        ServiceLoader<ResourceFactory> factories = ServiceLoader.load(ResourceFactory.class);
        for(ResourceFactory factory:factories) {
            this.resourceFactories.add(factory);
        }
    }

    /**
     * Return the resource factory which support the resource type
     * @param resourceType resource type
     * @return factory that can create given resource type
     * @throws IllegalArgumentException if not found the factory that can create given resource type
     */
    public ResourceFactory provideFactory(String resourceType) {
        for(ResourceFactory factory:resourceFactories) {
            if(factory.isSupportedResourceType(resourceType)) {
                return factory;
            }
        }
        throw new IllegalArgumentException("Not supported resource type [" + resourceType + "]");
    }
}
