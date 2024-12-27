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
 * This class is the starting context provider to get environment context.
 * ContextProvider is a wrapper of all configuration files and properties.
 *
 */
public class InitialEnvContextProvider implements ContextProvider {
    /**
     * Default context provider
     */
    private final ContextProvider ctxProvider;

    /**
     * Constructor of this class
     * @param env environment variables to build context path
     */
    public InitialEnvContextProvider(Map<String, String> env) {
        ctxProvider = ContextProviderRegistry.getInstance().getDefaultContextProvider(env);
    }

    @Override
    public void init(Map<String, String> env) {
        // do nothing
    }

    /**
     * Searches for the property with the specified key in this property list
     * @param propertyName the property name
     * @return the value in the property list of this provider. null if not exist property name
     * @throws IOException if a problem encountered
     * @see ContextProvider#getProperty(String)
     */
    @Override
    public String getProperty(String propertyName) throws IOException {
        return ctxProvider.getProperty(propertyName);
    }

    @Override
    public boolean isExist(String path) throws IOException {
        return ctxProvider.isExist(path);
    }

    /**
     * Searches the children of given context path and list their names
     * @param path the context path to list children
     * @return set of child names. 0 size set if context has no child
     * @throws IOException if a problem encountered
     * @see ContextProvider#listChildNames(String)
     */
    @Override
    public Set<String> listChildNames(String path) throws IOException {
        return ctxProvider.listChildNames(path);
    }

    /**
     * Check the context of given path has data
     * @param path the context path to check data existence.
     * @return true if context has data
     * @throws IOException if a problem encountered
     * @see ContextProvider#hasData(String)
     */
    @Override
    public boolean hasData(String path) throws IOException {
        return ctxProvider.hasData(path);
    }

    /**
     * Get an url of context data
     * @param path the context path to get data url.
     * @return url which can access the data. null if this context doesn't have data.
     * @throws IOException if a problem encountered
     * @see ContextProvider#getDataURL(String)
     */
    @Override
    public URL getDataURL(String path) throws IOException {
        return ctxProvider.getDataURL(path);
    }

    /**
     * Open data stream of context data
     * @param path the context path to open data stream.
     * @return input stream which contains the data. null if this context doesn't have data.
     * @throws IOException if a problem encountered
     * @see ContextProvider#openDataStream(String)
     */
    @Override
    public InputStream openDataStream(String path) throws IOException {
        return ctxProvider.openDataStream(path);
    }

    /**
     * Register context event watcher to watch context and sub context change event
     * @param path the context path to watch
     * @param watcher Event handler if change event occurred on the context or sub context
     * @throws IOException if a problem encountered
     * @see ContextProvider#watchContext(String, EventWatcher)
     */
    @Override
    public void watchContext(String path, EventWatcher watcher) throws IOException {
        ctxProvider.watchContext(path, watcher);
    }

    /**
     * Register property event watcher to watch property change event
     * @param propName the property name to watch
     * @param watcher Event handler if change event occurred on the property
     * @throws IOException if a problem encountered
     * @see EventWatcher
     */
    @Override
    public void watchProperty(String propName, EventWatcher watcher) throws IOException {
        ctxProvider.watchProperty(propName, watcher);
    }

    /**
     * Close this context provider
     * @throws Exception if a problem encountered during close
     * @see ContextProvider#close()
     */
    @Override
    public void close() throws Exception {
        ctxProvider.close();
    }
}
