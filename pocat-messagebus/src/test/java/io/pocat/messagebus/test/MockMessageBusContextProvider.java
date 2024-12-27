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

package io.pocat.messagebus.test;

import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import io.pocat.platform.messagebus.NamespaceContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MockMessageBusContextProvider implements MessageBusContextProvider {
    private final Map<String, NamespaceContext> namespacesMap = new HashMap<>();
    private final Map<String, EndpointContext> endpointsMap = new HashMap<>();
    @Override
    public NamespaceContext getNamespaceContext(String namespaceName) throws IOException {
        if(!namespacesMap.containsKey(namespaceName)) {
            throw new IOException("Namespace [" + namespaceName + "] does not exist.");
        }
        return namespacesMap.get(namespaceName);
    }

    @Override
    public EndpointContext getEndpointContext(String endpointName) throws IOException {
        if(!endpointsMap.containsKey(endpointName)) {
            throw new IOException("Endpoint [" + endpointName + "] does not exist.");
        }
        return endpointsMap.get(endpointName);
    }

    public void addNamespaceContext(NamespaceContext nameSpaceContext) {
        namespacesMap.put(nameSpaceContext.getName(), nameSpaceContext);
    }

    public void addEndpointContext(EndpointContext endpointContext) {
        endpointsMap.put(endpointContext.getName(), endpointContext);
    }
}
