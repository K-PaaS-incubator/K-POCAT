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

import java.io.IOException;

public class ServiceEnvironmentsImpl implements ServiceEnvironments {
    private final String containerId;
    private final String serviceId;
    private final String serviceName;
    private final ContextProvider provider;

    public ServiceEnvironmentsImpl(String containerId, String serviceId, String serviceName, ContextProvider provider) {
        this.containerId = containerId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.provider = provider;
    }
    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getEnvironmentProperty(String propertyName) throws IOException {
        return provider.getProperty(propertyName);
    }
}
