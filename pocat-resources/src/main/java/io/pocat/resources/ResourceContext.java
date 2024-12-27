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

package io.pocat.resources;

import java.util.Map;

/**
 * This interface represents resource configuration
 */
public interface ResourceContext {
    /**
     * Return resource path
     * @return resource path
     */
    String getResourcePath();

    /**
     * Return resource type
     * @return resource type
     */
    String getResourceType();

    /**
     * Return unmodifiable map of properties
     * @return map of properties
     */
    Map<String, String> getProperties();
}
