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
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents a context provider.
 * Context provider provides all accessible environment context for service.
 *
 * Environment Context means all things to run service
 * includes properties, configuration, libraries, files, and so on.
 */
public interface ContextProvider {
    /**
     * Initialize this provider
     * @param env environment variables
     */
    void init(Map<String, String> env);

    /**
     * Searches for the property with the specified key in this property list
     * @param propertyName the property name
     * @return the value in the property list of this provider. null if not exist property name
     * @throws IOException if a problem encountered
     */
    String getProperty(String propertyName) throws IOException;

    /**
     * Test the context of given path exist
     * @param path the context path to check existence.
     * @return true if context exist; false otherwise
     * @throws IOException if a problem encountered
     */
    boolean isExist(String path) throws IOException;

    /**
     * Test the context of given path has data
     * @param path the context path to check data existence.
     * @return true if context exist and has data; false otherwise
     * @throws IOException if a problem encountered
     */
    boolean hasData(String path) throws IOException;

    /**
     * Get an url of context data
     * @param path the context path to get data url.
     * @return url which can access the data. null if this context doesn't have data.
     * @throws IOException if a problem encountered
     */
    URL getDataURL(String path) throws IOException;

    /**
     * Open data stream of context data
     * @param path the context path to open data stream.
     * @return input stream which contains the data. null if this context doesn't have data.
     * @throws IOException if a problem encountered
     */
    InputStream openDataStream(String path) throws IOException;

    /**
     * Searches the children of given context path and list their names
     * @param path the context path to list children
     * @return set of child names. 0 size set if context has no child
     * @throws IOException if a problem encountered
     */
    Set<String> listChildNames(String path) throws IOException;

    /**
     * Register context event watcher to watch context and sub context change event
     * @param path the context path to watch
     * @param watcher Event handler if change event occurred on the context or sub context
     * @throws IOException if a problem encountered
     * @see EventWatcher
     */
    void watchContext(String path, EventWatcher watcher) throws IOException;

    /**
     * Register property event watcher to watch property change event
     * @param propName the property name to watch
     * @param watcher Event handler if change event occurred on the property
     * @throws IOException if a problem encountered
     * @see EventWatcher
     */
    void watchProperty(String propName, EventWatcher watcher) throws IOException;

    /**
     * Close this context provider
     * @throws Exception if a problem encountered during close
     */
    void close() throws Exception;
}
