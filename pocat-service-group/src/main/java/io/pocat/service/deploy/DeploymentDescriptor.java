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

package io.pocat.service.deploy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentDescriptor {
    private final String name;
    private final String serviceName;
    private final String serviceClass;

    private final Map<String, String> initParams = new HashMap<>();

    private final List<ConsumerGroupDescriptor> consumerGroupDescriptors = new ArrayList<>();
    private final List<ResourceDescriptor> resourceDescriptors = new ArrayList<>();
    private final List<OutboundChannelDescriptor> outboundChannelDescriptors = new ArrayList<>();

    private int maxWorker = Runtime.getRuntime().availableProcessors()*4;

    public DeploymentDescriptor(String name, String serviceName, String serviceClass) {
        this.name = name;
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
    }

    public String getName() {
        return name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public int getMaxWorker() {
        return maxWorker;
    }

    public void setMaxWorker(int maxWorker) {
        this.maxWorker = maxWorker;
    }

    public List<ConsumerGroupDescriptor> getConsumerGroupDescriptors() {
        return consumerGroupDescriptors;
    }

    public void addConsumerGroupDescriptor(ConsumerGroupDescriptor consumerGroupDescriptor) {
        this.consumerGroupDescriptors.add(consumerGroupDescriptor);
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public void addInitParam(String paramName, String paramValue) {
        this.initParams.put(paramName, paramValue);
    }

    public List<ResourceDescriptor> getResourceDescriptors() {
        return resourceDescriptors;
    }

    public void addResourceDescriptor(ResourceDescriptor resourceDescriptor) {
        this.resourceDescriptors.add(resourceDescriptor);
    }

    public List<OutboundChannelDescriptor> getChannelDescriptors() {
        return outboundChannelDescriptors;
    }

    public void addOutboundChannelDescriptor(OutboundChannelDescriptor outboundChannelDescriptor) {
        this.outboundChannelDescriptors.add(outboundChannelDescriptor);
    }
}
