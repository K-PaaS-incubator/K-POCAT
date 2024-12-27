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

import io.pocat.common.context.messagebus.EnvMessageBusContextProvider;
import io.pocat.common.context.resources.EnvResourceContextProvider;
import io.pocat.env.ContextProvider;
import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.messagebus.MessageBusConnectionFactory;
import io.pocat.platform.gateway.config.GatewayConfigType;
import io.pocat.platform.gateway.config.ServiceConfigType;
import io.pocat.platform.gateway.utils.stage.Stage;
import io.pocat.platform.gateway.utils.stage.StageManager;
import io.pocat.platform.gateway.utils.stage.StagedExecutorService;
import io.pocat.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Gateway {
    public static final String FILTER_EXECUTOR_NAME = "filter-stage";
    public static final String UPSTREAM_EXECUTOR_NAME = "upstream-stage";
    public static final String RESPONSE_EXECUTOR_NAME = "response-stage";
    public static final String DISPATCHER_EXECUTOR_NAME = "dispatcher-stage";

    public static final String GATEWAY_CONFIG_CONTEXT_PATH = "/env/gateway/config";
    private static final String[] STAGE_NAMES = new String[]{FILTER_EXECUTOR_NAME, UPSTREAM_EXECUTOR_NAME, RESPONSE_EXECUTOR_NAME, DISPATCHER_EXECUTOR_NAME};

    private final ContextProvider provider;
    private final String gatewayId;
    private ExecutorService executor;
    private Map<String, ServiceContainer> containers = new HashMap<>();
    private ResourceManager resourceManager;
    private ResponseHandler responseHandler;
    private boolean isRunning = true;
    private StageManager stageManager;
    private AccessLogger accessLogger;
    private MessageBusConnection connection;
    private GatewayConfigType gatewayConfig;
    private String responseChannel;

    public Gateway(ContextProvider provider) {
        this.gatewayId = UUID.randomUUID().toString().replaceAll("-", "");
        this.provider = provider;
    }

    public void init() {
        try (InputStream configStream = provider.openDataStream(GATEWAY_CONFIG_CONTEXT_PATH)) {
            if(configStream == null) {
                throw new IllegalArgumentException("Cannot find gateway config");
            }
            gatewayConfig = GatewayConfigType.newBuilder().build(configStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse config.", e);
        }
        this.executor = Executors.newFixedThreadPool(gatewayConfig.getWorkerPoolSize());
        Map<String, Stage> stages = new HashMap<>();
        for(String stageName:STAGE_NAMES) {
            stages.put(stageName, new StagedExecutorService(stageName, executor));
        }
        this.stageManager = new StageManager(stages);

        connection = new MessageBusConnectionFactory(new EnvMessageBusContextProvider(this.provider)).newConnection(this.executor);
        this.resourceManager = new ResourceManager(new EnvResourceContextProvider(this.provider));
        this.responseChannel = gatewayConfig.getResponseNamespace() + ":" + gatewayId;
        this.responseHandler = new ResponseHandler(this.gatewayId, connection);
        this.responseHandler.init(this.responseChannel, executor);
    }

    public void start() {
        this.isRunning = true;
        stageManager.start();
        try {
            this.responseHandler.start();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start response listener", e);
        }
        if(gatewayConfig.getServices() != null) {
            for (ServiceConfigType serviceConfig : gatewayConfig.getServices()) {
                ServiceContainer container = new ServiceContainer(this);
                container.init(serviceConfig);
                this.containers.put(serviceConfig.getName(), container);
            }
        }

        for (ServiceContainer container : this.containers.values()) {
            container.start();
        }
    }

    public void join() throws InterruptedException {
        while(!Thread.currentThread().isInterrupted()) {
            if(this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)) {
                break;
            }
        }
    }

    public void shutdown() {
        try {
            this.responseHandler.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(ServiceContainer container:this.containers.values()) {
            container.stop();
        }
        if(this.executor != null) {
            this.executor.shutdown();
        }
        try {
            this.provider.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContextProvider getContextProvider() {
        return this.provider;
    }

    public AccessLogger getAccessLogger() {
        return this.accessLogger;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public StageManager getExecutorManager() {
        return stageManager;
    }

    public MessageBusConnection getMessageBusConnection() {
        return connection;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public String getId() {
        return gatewayId;
    }

    public String getResponseNamespace() {
        return gatewayConfig.getResponseNamespace();
    }

    public String getResponseChannel() {
        return responseChannel;
    }
}
