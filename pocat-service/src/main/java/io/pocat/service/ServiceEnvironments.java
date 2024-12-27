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

public interface ServiceEnvironments {
    /**
     * Return unique id of the container that this service deployed
     * This id is distinguished with other containers with same name
     * @return unique id of the container
     */
    String getContainerId();

    /**
     * Return unique id of this service.
     * This id is distinguished with other services with same name
     * @return unique id of the service
     */
    String getServiceId();

    /**
     * Return service name of this service.
     * Service name will be same, if it used same deployment descriptor
     * @return service name
     */
    String getServiceName();

    /**
     * Search the property with the specified name in the environment properties
     * @param propertyName the property name
     * @return the value of property; null if not exist
     */
    String getEnvironmentProperty(String propertyName) throws IOException;
}
