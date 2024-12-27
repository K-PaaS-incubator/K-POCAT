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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ServiceDelegator {
    private Service service;
    private ExecutorService executor;
    private ServiceConfig config;
    private List<ConsumerGroup> consumerGroups = new ArrayList<>();

    public ServiceDelegator(Service service) {
        this.service = service;
    }

    public void init(ServiceConfig config) throws InitializeFailedException {
        this.config = config;
        this.service.init(config);
    }

    public void start() throws IOException {
        for(ConsumerGroup group:consumerGroups) {
            group.start();
        }
    }

    public void join() throws InterruptedException {
        boolean isShutdown = false;
        while(!isShutdown) {
            isShutdown = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }
    }

    public void stop() {
        for(ConsumerGroup group:consumerGroups) {
            group.stop();
        }
        executor.shutdown();
    }

    public void addConsumerGroup(ConsumerGroup consumerGroup) {
        this.consumerGroups.add(consumerGroup);
    }

    public void delegateExchange(MessageExchange exchange) throws ServiceException {
        try {
            service.serve(exchange);
        } catch (Throwable e) {
            throw new ServiceException(500, "Unknown error", e);
        }
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}
