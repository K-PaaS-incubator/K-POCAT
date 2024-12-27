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

import java.util.Set;

/**
 * This interface represents configuration for service initializing.
 */
public interface ServiceConfig {
    /**
     * Return a set of all the name of init parameters
     * @return a set of init parameter names
     */
    Set<String> getInitParameterNames();

    /**
     * Search the parameter with the specified name in init parameter list
     * @param paramName the parameter name
     * @return the value of parameter; null if not exist
     */
    String getInitParameter(String paramName);

    /**
     * Return resource with referenced name
     * @param refName Reference name
     * @return resource mapped reference name; null if not mapped
     */
    Object getResource(String refName) throws Exception;

    /**
     * Return environments of the service
     * @return environments of the service
     */
    ServiceEnvironments getServiceEnvironments();
}
