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

package io.pocat.platform.gateway;

import io.pocat.env.ContextProvider;
import io.pocat.env.InitialEnvContextProvider;

import java.util.Map;

public class GatewayBootstrap {
    public void boot(Map<String, String> env) {
        ContextProvider provider = new InitialEnvContextProvider(env);
        Gateway gateway = new Gateway(provider);
        try {
            gateway.init();
            gateway.start();
        } catch (Exception e) {
            gateway.shutdown();
            throw new IllegalStateException("Failed to start node", e);
        }

        // Add shutdown hook to stop gateway
        Runtime.getRuntime().addShutdownHook(new Thread(gateway::shutdown));

        try {
            // Wait to finish gateway
            gateway.join();
        } catch (InterruptedException ignored) {

        }

    }
}
