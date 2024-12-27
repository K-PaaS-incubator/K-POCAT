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

import java.util.Set;

/**
 * Implementation of {@link Namespace}
 */
class NamespaceImpl implements Namespace {
    private final String name;
    private final EndpointConnection endpointConnection;
    private final NamespaceContext ctx;

    public NamespaceImpl(String name, EndpointConnection endpointConnection, NamespaceContext ctx) {
        this.name = name;
        this.endpointConnection = endpointConnection;
        this.ctx = ctx;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public EndpointConnection getEndpointConnection() {
        return this.endpointConnection;
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.ctx.getPropertyNames();
    }

    @Override
    public String getProperty(String propName) {
        return this.ctx.getProperty(propName);
    }

    @Override
    public boolean hasProperty(String propName) {
        return this.ctx.getPropertyNames().contains(propName);
    }
}
