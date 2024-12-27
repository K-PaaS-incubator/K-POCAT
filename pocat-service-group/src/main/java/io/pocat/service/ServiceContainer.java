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

import io.pocat.common.context.messagebus.EnvMessageBusContextProvider;
import io.pocat.common.context.resources.EnvResourceContextProvider;
import io.pocat.env.ContextProvider;
import io.pocat.env.EventType;
import io.pocat.env.EventWatcher;
import io.pocat.platform.messagebus.MessageBusConnectionFactory;
import io.pocat.resources.ResourceManager;
import io.pocat.service.deploy.DeploymentDescriptor;
import io.pocat.service.deploy.DeploymentDescriptorBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class ServiceContainer {
    private final ExecutorService serviceExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    private final Map<String, ServiceDelegator> delegators = new HashMap<>();

    private final String containerId;
    private final ContextProvider provider;

    private MessageBusConnectionFactory messageBusConnectionFactory;
    private ResourceManager resourceManager;

    public ServiceContainer(String containerName, ContextProvider provider) {
        // todo more unique id
        this.containerId = UUID.randomUUID().toString().replaceAll("-", "");
        this.provider = provider;
    }

    public void start() throws IOException {
        this.resourceManager = new ResourceManager(new EnvResourceContextProvider(provider));
        this.messageBusConnectionFactory  = new MessageBusConnectionFactory(new EnvMessageBusContextProvider(provider));

        Set<String> deployNames = this.provider.listChildNames("/env/deploy");

        for(String deployName:deployNames) {
            deploy(deployName);
        }

        for(ServiceDelegator delegator: delegators.values()) {
            serviceExecutor.execute(() -> {
                try {
                    delegator.start();
                    delegator.join();
                } catch (IOException e) {
                    delegator.stop();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        this.provider.watchContext("/env/deploy", new DeploymentWatcher());
        this.provider.watchContext("/env/services", new ServiceWatcher());
        this.provider.watchContext("/env/context/resources", new ResourceWatcher());
    }

    public void stop() {
        for(ServiceDelegator delegator: delegators.values()) {
            delegator.stop();
        }
        serviceExecutor.shutdown();
        try {
            this.provider.close();
        } catch (Exception ignored) {

        }
    }

    public void join() throws InterruptedException {
        while(!Thread.currentThread().isInterrupted()) {
            if(serviceExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)) {
                break;
            }
        }
    }

    private void deploy(String deployName) throws IOException {
        DeploymentDescriptor deployDesc;

        URL deploymentURL = this.provider.getDataURL(ContainerContextProviderFactory.DEPLOY_CONTEXT_ROOT_PATH + "/" + deployName);
        deployDesc = new DeploymentDescriptorBuilder().buildFrom(deploymentURL);
        ServiceDelegator delegator = new ServiceDeployer(this).deploy(deployDesc);
        delegators.put(deployName, delegator);
    }

    private void undeploy(String deployName) {
        ServiceDelegator delegator = delegators.remove(deployName);
        delegator.stop();
    }

    public MessageBusConnectionFactory getMessageBusConnectionFactory() {
        return this.messageBusConnectionFactory;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public ContextProvider getContextProvider() {
        return this.provider;
    }

    public String getContainerId() {
        return containerId;
    }

    private static class DeploymentWatcher implements EventWatcher {
        @Override
        public void handleEvent(EventType type, String path) {
            switch (type) {
                case CREATE:
                    break;
                case UPDATE:

                    break;
                case DELETE:
                    break;
            }
        }
    }

    private static class ServiceWatcher implements EventWatcher {
        @Override
        public void handleEvent(EventType type, String key) {

        }
    }

    private static class ResourceWatcher implements EventWatcher {
        @Override
        public void handleEvent(EventType type, String key) {

        }
    }
}
