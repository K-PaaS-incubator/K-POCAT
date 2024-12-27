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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ServiceConfigImpl implements ServiceConfig {
    private final Map<String, String> initParams;
    private final ServiceEnvironments serviceEnvironments;
    private final Map<String,ResourceReferrer> resourceReferrer;

    public ServiceConfigImpl(Map<String, String> initParams, Map<String,ResourceReferrer> resourceReferrer, ServiceEnvironments serviceEnvironments) {
        this.initParams = initParams;
        this.resourceReferrer = resourceReferrer;
        this.serviceEnvironments = serviceEnvironments;
    }

    @Override
    public Set<String> getInitParameterNames() {
        return Collections.unmodifiableSet(this.initParams.keySet());
    }

    @Override
    public String getInitParameter(String paramName) {
        return this.initParams.get(paramName);
    }

    @Override
    public Object getResource(String refName) throws Exception {
        ResourceReferrer referrer = this.resourceReferrer.get(refName);
        if(referrer == null) {
            return null;
        }
        return referrer.getResource();
    }

    @Override
    public ServiceEnvironments getServiceEnvironments() {
        return serviceEnvironments;
    }
}
