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
import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.messagebus.MessageDeliveryHandler;
import io.pocat.service.deploy.ConsumerGroupDescriptor;
import io.pocat.service.deploy.DeploymentDescriptor;
import io.pocat.service.deploy.OutboundChannelDescriptor;
import io.pocat.service.deploy.ResourceDescriptor;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ServiceDeployer {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("");
    private static final String SERVICE_HOME_CONTEXT_PATH = "/env/services";
    private ServiceContainer serviceContainer;

    public ServiceDeployer(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

    public ServiceDelegator deploy(DeploymentDescriptor deployDesc) {
        String deployId = UUID.randomUUID().toString();
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ServiceClassLoader serviceClassLoader = new ServiceClassLoader(getServiceLibs(deployDesc.getServiceName()), this.getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(serviceClassLoader);

            Class<?> serviceClass = Class.forName(deployDesc.getServiceClass(), true, serviceClassLoader);
            Service service = (Service) serviceClass.getConstructor().newInstance();
            ServiceDelegator delegator = new ServiceDelegator(service);

            ExecutorService executor = Executors.newFixedThreadPool(deployDesc.getMaxWorker());
            delegator.setExecutor(executor);

            MessageBusConnection messageBusConnection = serviceContainer.getMessageBusConnectionFactory().newConnection(executor);

            Map<String, ResourceReferrer> resourceReferrers = new HashMap<>();
            for(ResourceDescriptor descriptor:deployDesc.getResourceDescriptors()) {
                ResourceReferrer referrer = new ResourceManagerReferrer(descriptor.getResourceName(), serviceContainer.getResourceManager());
                resourceReferrers.put(descriptor.getRefName(), referrer);
            }

            for(OutboundChannelDescriptor descriptor: deployDesc.getChannelDescriptors()) {
                ResourceReferrer referrer = new PublisherReferrer(new MessageChannelPublisherImpl(descriptor.getChannelName(), messageBusConnection));
                resourceReferrers.put(descriptor.getRefName(), referrer);
            }

            ServiceEnvironments environments = new ServiceEnvironmentsImpl(
                                                    serviceContainer.getContainerId(),
                                                    deployId,
                                                    deployDesc.getName(),
                                                    serviceContainer.getContextProvider()
            );

            ServiceConfig serviceConfig = new ServiceConfigImpl(deployDesc.getInitParams(), resourceReferrers, environments);

            for(ConsumerGroupDescriptor desc:deployDesc.getConsumerGroupDescriptors()) {
                String name = desc.getName();
                String consumerGroupName = updateVariables(name, deployId, desc.getName());
                for(String channel: desc.getChannelNames()) {
                    // todo check variables
                    messageBusConnection.bind(consumerGroupName, updateVariables(channel, deployId, desc.getName()));
                }

                MessageDeliveryHandler deliveryHandler = (msgSource, messageHeader, payload) -> {
                    // todo AccessLogger.getInstance().log();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            MessageExchangeImpl exchange = new MessageExchangeImpl(msgSource, messageHeader, payload, desc.isTapType());

                            try {
                                delegator.delegateExchange(exchange);
                            } catch (ServiceException e) {
                                e.printStackTrace();
                            }
                            if(exchange.isSetReply()) {
                                try {
                                    Map<String, String> replyHeaders = new HashMap<>(exchange.getReplyHeaders());
                                    replyHeaders.put("Tx-Id", messageHeader.get("Tx-Id"));
                                    replyHeaders.put("Status-Code", String.valueOf(0));
                                    messageBusConnection.publish(exchange.getRequestHeader("Reply-To"), replyHeaders, exchange.getReplyPayload());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // todo AccessLogger.getInstance().log();
                            }
                        }
                    });
                };
                delegator.addConsumerGroup(new ConsumerGroup(messageBusConnection, consumerGroupName, deliveryHandler));
            }
            delegator.init(serviceConfig);
            return delegator;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        return null;
    }

    private String updateVariables(String name, String serviceId, String serviceName) {
        String varName = name;

        varName = varName.replaceAll("\\$\\{container:id}", serviceContainer.getContainerId());
        varName = varName.replaceAll("\\$\\{service:id}", serviceId);
        varName = varName.replaceAll("\\$\\{service:name}", serviceName);
        return varName;
    }

    private URL[] getServiceLibs(String serviceName) throws IOException {
        ContextProvider provider = this.serviceContainer.getContextProvider();
        String serviceLibHomePath = SERVICE_HOME_CONTEXT_PATH + "/" + serviceName + "/libs";
        if(!provider.isExist(serviceLibHomePath)) {
            throw new IOException("Context [" + serviceLibHomePath + "] does not exist");
        }
        Set<String> serviceLibNames = provider.listChildNames(serviceLibHomePath);
        List<URL> serviceLibUrls = new ArrayList<>();
        for (String serviceLibName : serviceLibNames) {
            if (provider.hasData(serviceLibHomePath + "/" + serviceLibName)) {
                serviceLibUrls.add(provider.getDataURL(serviceLibHomePath + "/" + serviceLibName));
            }
        }
        return serviceLibUrls.toArray(new URL[0]);
    }
}
