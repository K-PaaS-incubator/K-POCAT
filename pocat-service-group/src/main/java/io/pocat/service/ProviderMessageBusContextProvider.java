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
import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import io.pocat.platform.messagebus.NamespaceContext;

import java.io.IOException;

public class ProviderMessageBusContextProvider implements MessageBusContextProvider {
    public ProviderMessageBusContextProvider(ContextProvider provider) {
    }

    @Override
    public NamespaceContext getNamespaceContext(String namespaceName) throws IOException {
        return null;
    }

    @Override
    public EndpointContext getEndpointContext(String endpointName) throws IOException {
        return null;
    }
}
