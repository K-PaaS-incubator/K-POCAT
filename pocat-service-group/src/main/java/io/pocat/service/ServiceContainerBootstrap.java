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

package io.pocat.service;

import io.pocat.env.ContextProvider;
import io.pocat.env.InitialEnvContextProvider;
import java.util.Map;

/**
 * Bootstrap of Service node
 */
public class ServiceContainerBootstrap {
    private static final String CONTAINER_NAME_ENV_NAME = "io.pocat.service.container.name";

    /**
     * Bootstrap of service group
     * @param env environments
     */
    public void boot(Map<String, String> env) {
        ContextProvider provider = new InitialEnvContextProvider(env);
        ServiceContainer serviceContainer = new ServiceContainer(env.get(CONTAINER_NAME_ENV_NAME), provider);
        try {
            serviceContainer.start();
        } catch (Exception e) {
            serviceContainer.stop();
            throw new IllegalStateException("Failed to start node", e);

        }
        // Add shutdown hook to stop container
        Runtime.getRuntime().addShutdownHook(new Thread(serviceContainer::stop));

        try {
            // Wait to finish all services
            serviceContainer.join();
        } catch (InterruptedException ignored) {

        }

    }
}
