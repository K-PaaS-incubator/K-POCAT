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
 * This interface represents a factory for create resource
 */
public interface ResourceFactory {
    /**
     * Check resource type is acceptable to this factory
     * @param resourceType type of resource
     * @return true if resource type is acceptable; false otherwise
     */
    boolean isSupportedResourceType(String resourceType);

    /**
     * Create resource instance
     *
     * @param props properties to create resource
     * @return the resource created
     * @throws Exception if it encounters a problem at resource creation time.
     */
    Object createResource(Map<String, String> props) throws Exception;
}
