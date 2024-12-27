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
 * This interface represents namespace configuration.
 */
public interface NamespaceContext {
    /**
     * Get namespace name
     * @return the namespace name
     */
    String getName();

    /**
     * Get endpoint name which referenced by this namespace
     * @return an endpoint name
     */
    String getEndpointRef();

    /**
     * Get endpoint context which this namespace originated from
     * @return an endpoint context
     */
    EndpointContext getEndpointContext();

    /**
     * Returns a set of all the names of arguments in this context.
     * @return a map of namespace arguments
     */
    Set<String> getPropertyNames();

    /**
     * Searches for the property with the specified key in this property list
     * @param name the property name
     * @return the value in the property list of this provider. null if not exist property name
     */
    String getProperty(String name);
}
