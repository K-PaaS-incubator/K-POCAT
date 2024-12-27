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
 * This interface represents namespace of message bus
 */
public interface Namespace {
    /**
     * Get namespace name
     * @return name of namespace
     */
    String getName();

    /**
     * Get endpoint connection which this namespace originated from
     * @return endpoint connection
     */
    EndpointConnection getEndpointConnection();

    /**
     * Returns a set of all the keys in this property list.
     * @return a set of all the keys in this property list
     */
    Set<String> getPropertyNames();

    /**
     * Searches for the property with the specified name in this property list.
     * @param propName the property name
     * @return the value in this property list with the specified name value. null if name is not found
     */
    String getProperty(String propName);

    /**
     * Searches for the property with the specified name in this property list.
     * @param propName the property name
     * @param defaultValue a default value
     * @return the value in this property list with the specified key value. default value if name is not found
     */
    default String getProperty(String propName, String defaultValue) {
        return hasProperty(propName)?getProperty(propName):defaultValue;
    }

    /**
     * Tests if the specified name in this property list
     * @param propName the property name
     * @return true if and only if the specified name is in this hashtable; false otherwise.
     */
    boolean hasProperty(String propName);
}
