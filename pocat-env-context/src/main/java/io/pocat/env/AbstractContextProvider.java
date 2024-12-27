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
import java.util.*;

/**
 * Abstract class of Context provider
 * This abstraction contains about tree operations.
 */
public abstract class AbstractContextProvider implements ContextProvider {
    public static final String CONTEXT_PATH_SEPARATOR = "/";

    private Map<String, String> env;
    private final ContextTree ctxTree;

    /**
     * Constructor
     */
    protected AbstractContextProvider() {
        ctxTree = new ContextTree();
    }

    /**
     * Test the context of given path exist
     * @param path the context path to check existence.
     * @return true if context exist; false otherwise
     */
    public boolean isExist(String path) {
        Context node = ctxTree.findContext(path);
        return node != null;
    }

    /**
     * Test the context of given path has data
     * @param path the context path to check data existence.
     * @return true if context has data url; false otherwise
     */
    @Override
    public boolean hasData(String path) {
        Context node = ctxTree.findContext(path);
        return node != null && node.getUrl() != null;
    }

    /**
     * Get an url of context data
     * @param path the context path to get data url.
     * @return url which can access the data. null if this context doesn't have data.
     */
    @Override
    public URL getDataURL(String path) {
        Context context = ctxTree.findContext(path);
        if(context == null) {
            return null;
        }
        return context.getUrl();
    }

    /**
     * Open data stream of context data
     * @param path the context path to open data stream.
     * @return input stream which contains the data. null if this context doesn't have data.
     * @throws IOException if a problem encountered during open url stream
     */
    @Override
    public InputStream openDataStream(String path) throws IOException {
        URL url = getDataURL(path);
        if(url == null) {
            return null;
        }
        return url.openStream();
    }

    /**
     * Searches the children of given context path and list their names
     * @param path the context path to list children
     * @return set of child names. 0 size set if context has no child
     */
    @Override
    public Set<String> listChildNames(String path) {
        Context context = ctxTree.findContext(path);
        if(context == null) {
            return Collections.emptySet();
        }
        return context.listChildNames();
    }

    /**
     * Register context event watcher to watch context and sub context change event
     * @param path the context path to watch
     * @param watcher Event handler if change event occurred on the context or sub context
     * @throws IOException if the context not found
     * @see EventWatcher
     */
    @Override
    public void watchContext(String path, EventWatcher watcher) throws IOException {
        Context ctx = ctxTree.findContext(path);
        if(ctx == null) {
            throw new IOException("Context does not exist.");
        }
        ctx.addWatcher(watcher);
    }

    /**
     * Initialize this provider
     * @param env environment variables
     */
    @Override
    public void init(Map<String, String> env) {
        this.env = env;
        init();
    }

    /**
     * Return environment variables of this provider
     * @return environment variables of this provider
     */
    protected Map<String, String> getEnv() {
        return env;
    }

    /**
     * Return context tree of this provider
     * @return context tree of this provider
     */
    protected ContextTree getContextTree() {
        return ctxTree;
    }

    /**
     * Initialize this provider
     */
    protected abstract void init();

    /**
     * Context data holder
     */
    protected static class Context {
        private final ContextTree tree;
        /**
         * Path of this node
         */
        private final String ctxPath;
        /**
         * Child names of this node
         */
        private final Set<String> childrenNames = new HashSet<>();

        /**
         * Data url of this node
         */
        private URL url;

        private final Set<EventWatcher> contextEventWatchers = new HashSet<>();

        /**
         * Constructor
         * @param tree tree that this context added
         * @param ctxPath path of this node
         */
        public Context(ContextTree tree, String ctxPath) {
            this(tree, ctxPath, null);
        }

        /**
         * Constructor with data initialize
         * @param tree tree that this context added
         * @param ctxPath path of this node
         * @param url data url of this node
         */
        public Context(ContextTree tree, String ctxPath, URL url) {
            this.tree = tree;
            this.ctxPath = ctxPath;
            this.url = url;
        }

        /**
         * Return path of this node
         * @return path of this node
         */
        public String getContextPath() {
            return ctxPath;
        }

        /**
         * Register child to this node
         * @param child child context to register
         */
        public void addChild(Context child) {
            childrenNames.add(child.getName());
            if(child.getUrl() != null) {
                handleEvent(EventType.CREATE, child.getContextPath());
            }
        }

        /**
         * Remove child name
         * @param childName child name to remove
         */
        public void removeChild(String childName) {
            childrenNames.remove(childName);
            handleEvent(EventType.DELETE, this.ctxPath + "/" + childName);
        }

        /**
         * List child names of this node
         * @return child names of this node
         */
        public Set<String> listChildNames() {
            return Collections.unmodifiableSet(childrenNames);
        }

        /**
         * Return data url of this node
         * @return data url of this node
         */
        public URL getUrl() {
            return url;
        }

        /**
         * Update data url of this node
         * @param url data url of this node
         */
        public void setUrl(URL url) {
            this.url = url;
            handleEvent(EventType.UPDATE, ctxPath);
        }

        /**
         * Get parent context
         * @return parent context
         */
        public Context getParent() {
            return tree.findContext(this.ctxPath.substring(0, this.ctxPath.lastIndexOf("/")));
        }

        /**
         * Add event watcher
         * @param watcher watcher to add
         */
        public void addWatcher(EventWatcher watcher) {
            this.contextEventWatchers.add(watcher);
        }

        /**
         * Remove event watcher
         * @param watcher watcher to remove
         */
        public void removeWatcher(EventWatcher watcher) {
            this.contextEventWatchers.remove(watcher);
        }

        /**
         * Return set of watcher in current context
         * @return set of watcher in current context
         */
        public Set<EventWatcher> listWatchers() {
            return Collections.unmodifiableSet(this.contextEventWatchers);
        }

        private void handleEvent(EventType eventType, String path) {
            for(EventWatcher watcher:contextEventWatchers) {
                watcher.handleEvent(eventType, path);
            }
            if(!ctxPath.equalsIgnoreCase("/")) {
                getParent().handleEvent(eventType, path);
            }
        }

        private String getName() {
            return ctxPath.substring(ctxPath.lastIndexOf("/") + 1);
        }
    }

    /**
     * Context holder
     */
    protected static class ContextTree {
        private static final String CONTEXT_SEPARATOR = "/";
        /**
         * Node registry
         */
        private final Map<String, Context> contexts = new HashMap<>();
        /**
         * Root node of tree
         */
        private final Context rootContext = new Context(this, CONTEXT_SEPARATOR);

        /**
         * Constructor
         */
        public ContextTree() {
            contexts.put(CONTEXT_SEPARATOR, rootContext);
        }
        /**
         * Add context to path
         * @param context context to add
         */
        public void addContext(Context context) {
            String path = context.getContextPath();
            if(contexts.containsKey(path)) {
                throw new IllegalStateException("Already exist");
            }
            Context parent = getParent(path);
            // register child to parent
            parent.addChild(context);
            contexts.put(path, context);
        }

        /**
         * Find context at given path
         * @param path path to find context
         * @return found context; null if not exist
         */
        public Context findContext(String path) {
            if(path.isEmpty()) {
                return rootContext;
            }
            return contexts.get(path);
        }

        /**
         * Remove context and sub contexts at given path
         * @param path path to remove context
         */
        public void removeContext(String path) {
            Context ctx = contexts.get(path);
            if(ctx != null) {
                for(String childName:ctx.listChildNames()) {
                    removeContext(path + CONTEXT_SEPARATOR + childName);
                }

                Context parent = getParent(path);
                parent.removeChild(getName(path));
            }
        }

        /**
         * Return set of all context path in this tree
         * @return set of all context path in this tree
         */
        public Set<String> listAllPath() {
            return contexts.keySet();
        }

        /**
         * Find parent node
         * @param path path to find parent node
         * @return parent node; cannot be null
         */
        private Context getParent(String path) {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            if(parentPath.isEmpty()) {
                // return root if search reached to root path
                return rootContext;
            }
            Context parentContext = findContext(parentPath);
            if(parentContext == null) {
                // If parent node does not exist, create.
                parentContext = new Context(this, parentPath);
                addContext(parentContext);
            }
            return parentContext;
        }

        /**
         * Extract name from path
         * @param path path to extract name
         * @return name
         */
        private String getName(String path) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }
}
