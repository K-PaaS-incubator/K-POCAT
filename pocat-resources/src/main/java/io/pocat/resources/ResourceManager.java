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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controller of resources
 */
public class ResourceManager {
    /**
     * Provider of resource context
     */
    private final ResourceContextProvider ctxProvider;
    /**
     * Resource registry with resource path as a key
     */
    private final Map<String, Object> resourceRegistry = new HashMap<>(); // todo How to use Proxy object not a real object?
    /**
     * Resource factory provider
     */
    private final ResourceFactoryProvider factoryProvider = new ResourceFactoryProvider();
    /**
     * Resource creation lock
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor
     * @param ctxProvider provider of resource context
     */
    public ResourceManager(ResourceContextProvider ctxProvider) {
        this.ctxProvider = ctxProvider;
    }

    /**
     * Find resource with specified path
     * @param resourcePath resource path
     * @return resource instance from registry; null if resource context not found
     * @throws Exception if it encounters a problem at resource creation time.
     */
    public Object getResource(String resourcePath) throws Exception {
        if(!resourceRegistry.containsKey(resourcePath)) {
            this.lock.lock();
            try {
                if (!resourceRegistry.containsKey(resourcePath)) {
                    resourceRegistry.put(resourcePath, createResource(resourcePath));
                }
            } catch (Exception e) {
                // do not retry to create.
                resourceRegistry.put(resourcePath, null);
                throw e;
            } finally {
                this.lock.unlock();
            }
        }
        return resourceRegistry.get(resourcePath);
    }

    /**
     * Update resource with specified path
     * @param resourcePath resource path
     * @throws Exception if it encounters a problem at updated resource object creation time.
     */
    public void updateResource(String resourcePath) throws Exception {
        this.lock.lock();
        try {
            // todo Does reloading really need?
            factoryProvider.reload();
            resourceRegistry.remove(resourcePath);
            resourceRegistry.put(resourcePath, createResource(resourcePath));
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Close all resources
     */
    public void close() {
        // todo How to close resource?
    }

    /**
     * Create resource via resource factory that found by resource type
     * @param resourcePath resource path to create
     * @return resource instance,
     * @throws Exception if it encounters a problem at resource creation time
     */
    private Object createResource(String resourcePath) throws Exception {
        ResourceContext resourceCtx = this.ctxProvider.getResourceContext(resourcePath);
        ResourceFactory factory = factoryProvider.provideFactory(resourceCtx.getResourceType());
        return factory.createResource(resourceCtx.getProperties());
    }
}
