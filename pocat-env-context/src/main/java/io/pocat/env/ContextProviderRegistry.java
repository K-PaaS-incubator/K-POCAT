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

package io.pocat.env;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry for context Provider
 */
final class ContextProviderRegistry {
    /**
     * Property name to set context factory provider
     */
    public static final String CONTEXT_PROVIDER_FACTORY_PROP_NAME = "io.pocat.context.provider.factory";

    /**
     * For singleton
     */
    private static final ContextProviderRegistry INSTANCE = new ContextProviderRegistry();

    /**
     * Context Provider registry with env as a key
     */
    private final Map<Map<String, String>, ContextProvider> providers = new HashMap<>();

    /**
     * Lock to create provider at same time
     */
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * Get a singleton instance of ContextProviderRegistry
     * @return Singleton Instance of ContextProviderRegistry
     */
    public static ContextProviderRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Get a default ContextProvider with given environments variables
     * if not created, Create new Context Provider with given factory by the property {@value CONTEXT_PROVIDER_FACTORY_PROP_NAME}
     * @param env environment variables to build context path
     * @return context provider instance. null if already failed to create.
     */
    public ContextProvider getDefaultContextProvider(Map<String, String> env) {
        if(!providers.containsKey(env)) {
            lock.lock();
            try {
                if(!providers.containsKey(env)) {
                    try {
                        ContextProvider provider = createDefaultContextProvider(env);
                        providers.put(env, provider);
                    } catch (Exception e) {
                        // No need to try to create again
                        providers.put(env, null);
                        throw e;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        ContextProvider provider = providers.get(env);
        if(provider == null) {
            // Already failed to create
            throw new IllegalStateException("Failed to create provider factory.");
        }
        return provider;
    }

    /**
     * Create default context provider with given environment variables
     * @param env environment variables to build context path
     * @return created context provider instance with given environment variables
     */
    private ContextProvider createDefaultContextProvider(Map<String, String> env) {
        String providerFactoryClazzName = env.get(CONTEXT_PROVIDER_FACTORY_PROP_NAME);
        if(providerFactoryClazzName == null) {
            throw new IllegalStateException("Need to specify context provider factory class name [" + CONTEXT_PROVIDER_FACTORY_PROP_NAME + "]");
        }
        try {
            Class<?> providerFactoryClazz = Class.forName(providerFactoryClazzName);
            Constructor<?> constructor = providerFactoryClazz.getConstructor();
            ContextProviderFactory factory = (ContextProviderFactory) constructor.newInstance();
            return factory.createProvider(env);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create provider factory [" + providerFactoryClazzName + "]", e);
        }
    }
}
