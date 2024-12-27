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

package io.pocat.env;

import java.io.IOException;
import java.util.Map;

/**
 * This interface represents a factory that creates a context provider.
 *
 * @see ContextProvider
 */
public interface ContextProviderFactory {
    /**
     * Create {@link ContextProvider} instance
     * @param env Environment variables which will be used to create ContextProvider
     * @return ContextProvider instance
     * @throws IOException if it encounters a problem during create provider.
     */
    ContextProvider createProvider(Map<String, String> env) throws IOException;
}
