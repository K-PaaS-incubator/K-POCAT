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

package io.pocat.common.context;

import io.pocat.env.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Provides an abstract class to be subclassed to create directory based Implementation of {@link ContextProvider}
 */
public abstract class DirEnvContextProvider extends AbstractContextProvider {
    public static final String ENDPOINT_CONTEXT_ROOT_PATH = "/env/context/messagebus/endpoints";
    public static final String ENDPOINT_BASE_NODE_PATH = "/context/endpoints/endpoint";
    public static final String ENDPOINT_KEY_NODE_PATH = "name";

    public static final String NAMESPACE_CONTEXT_ROOT_PATH = "/env/context/messagebus/namespaces";
    public static final String NAMESPACE_BASE_NODE_PATH = "/context/namespaces/namespace";
    public static final String NAMESPACE_KEY_NODE_PATH = "name";

    public static final String RESOURCE_CONTEXT_ROOT_PATH = "/env/context/resources";
    public static final String RESOURCE_BASE_NODE_PATH = "/context/resources/resource";
    public static final String RESOURCE_KEY_NODE_PATH = "name";

    protected static final DirContextType ENDPOINT_CTX_TYPE = new DirContextType(ENDPOINT_CONTEXT_ROOT_PATH, ENDPOINT_BASE_NODE_PATH, ENDPOINT_KEY_NODE_PATH);
    protected static final DirContextType NAMESPACE_CTX_TYPE = new DirContextType(NAMESPACE_CONTEXT_ROOT_PATH, NAMESPACE_BASE_NODE_PATH, NAMESPACE_KEY_NODE_PATH);
    protected static final DirContextType RESOURCE_CTX_TYPE = new DirContextType(RESOURCE_CONTEXT_ROOT_PATH, RESOURCE_BASE_NODE_PATH, RESOURCE_KEY_NODE_PATH);

    protected static final String CONTEXT_URL_PROTOCOL = "context";

    private final Map<String, EventWatcher> propWatchers = new HashMap<>();
    private final ContextFileWatcher watcher;

    private Properties properties;

    /**
     * Constructor
     * @throws IOException if failed to create file watcher
     */
    protected DirEnvContextProvider() throws IOException {
        watcher = new ContextFileWatcher();
        watcher.start();
    }

    /**
     * Return the value in the property list of this provider.
     * @param propertyName the property name
     * @return the value in the property list of this provider. null if not exist property name
     */
    @Override
    public String getProperty(String propertyName) {
        if(properties == null) {
            return null;
        }
        return properties.getProperty(propertyName);
    }

    /**
     * Register property event watcher to watch property change event
     * @param propName the property name to watch
     * @param watcher Event handler if change event occurred on the property
     * @see EventWatcher
     */
    @Override
    public void watchProperty(String propName, EventWatcher watcher) {
        this.propWatchers.put(propName, watcher);
    }

    /**
     * Close this context provider
     */
    @Override
    public void close() throws IOException {
        watcher.stop();
    }

    /**
     * Validate given directory.
     * If recommended, given directory must exist.
     * If mkdirs is true, try to make given directory, recommended or not
     * @param dirPath directory path to validate
     * @param recommended directory must exist
     * @param mkdirs make directory if not exist
     * @throws IllegalArgumentException if dirPath is empty, dirPath is file, recommended directory does not exist or failed to make directory.
     */
    protected void validateDirectory(String dirPath, boolean recommended, boolean mkdirs) {
        if(dirPath == null || dirPath.isEmpty()) {
            throw new IllegalArgumentException("Empty path.");
        }

        File dir = new File(dirPath);
        if(dir.isFile()) {
            throw new IllegalArgumentException("Not a directory [" + dir.getAbsolutePath() + "].");
        }

        if(!dir.exists() && mkdirs) {
            if(!dir.mkdirs()) {
                throw new IllegalArgumentException("Failed to create directory [" + dir.getAbsolutePath() + "]");
            }
        }
        if(!dir.exists() && recommended) {
            throw new IllegalArgumentException("Directory [" + dirPath + "] does not exist.");
        }
    }

    /**
     * Load environment properties.
     *
     * @param propertyFilePath environment properties file path
     * @throws IllegalArgumentException if it encounters a problem at load properties file.
     */
    protected void reloadProperties(String propertyFilePath) {
        this.properties = new Properties();
        File propFile = new File(propertyFilePath);
        if(propFile.exists()) {
            try (InputStream propStream = new FileInputStream(propFile)) {
                properties.load(propStream);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid properties file [" + propFile.getAbsolutePath() + "]");
            }
        }
        for(String propName:this.properties.stringPropertyNames()) {
            if(propWatchers.containsKey(propName)) {
                propWatchers.get(propName).handleEvent(EventType.UPDATE, propName);
            }
        }
    }

    /**
     * Load context file and add to tree
     * If context file does not exist, context tree will empty
     * @param contextFilePath xml context file path
     * @throws IllegalArgumentException if it encounters a problem during load context file.
     */
    protected void reloadContext(String contextFilePath) {
        File contextFile = new File(contextFilePath);
        if(!contextFile.exists()) {
            throw new IllegalArgumentException("Context file [" + contextFilePath + "] not found");
        }
        try {
            DirContextType[] splitTypes = new DirContextType[] {ENDPOINT_CTX_TYPE, NAMESPACE_CTX_TYPE, RESOURCE_CTX_TYPE};

            for(DirContextType splitType:splitTypes) {
                updateTree(splitType.getContextRoot(), splitDocument(contextFile, splitType.getContextRoot(), splitType.getBaseNodePath(), splitType.getKeyNodePath()));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load context file [" + contextFilePath + "]", e);
        }
    }

    /**
     * Add all urls to context tree
     * @param contextRoot root of context to update
     * @param urls url list to add
     */
    protected void updateTree(String contextRoot, List<URL> urls) {
        // todo Find and remove the removed context
        for(URL url: urls) {
            String path = url.getPath();
            Context context = getContextTree().findContext(path);
            if(context != null) {
                context.setUrl(url);
            } else {
                getContextTree().addContext(new Context(getContextTree(), url.getPath(), url));
            }
        }
    }

    /**
     * Split given document to node based document
     * @param file file to split
     * @param contextRootPath root path of returned url
     * @param nodePath path of base node to split document; this node will be a root node of document after split
     * @param keyNodePath key of split document
     * @return list of split document url. url path is the string that the key is added to the given context root path
     * @throws Exception if it encounters a problem during split document.
     */
    protected List<URL> splitDocument(File file, String contextRootPath, String nodePath, String keyNodePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        Element root = document.getDocumentElement();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        // Add Xml header
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        XPath xPath = XPathFactory.newInstance().newXPath();
        // Get nodes of context
        List<URL> urls = new ArrayList<>();
        NodeList children = (NodeList) xPath.evaluate(nodePath, root, XPathConstants.NODESET);
        if(children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                // Get key node value from node
                String key = (String) xPath.evaluate(keyNodePath, node, XPathConstants.STRING);
                if(key == null || key.isEmpty()) {
                    throw new IOException("Key node [" + keyNodePath + "] is not exist.");
                }

                StringWriter sw = new StringWriter();
                t.transform(new DOMSource(node), new StreamResult(sw));
                byte[] data = sw.toString().getBytes(StandardCharsets.UTF_8);

                // Create url and add handler to handle url
                URLStreamHandler handler = new URLStreamHandler() {
                    @Override
                    public URLConnection openConnection(URL u) {
                        return new URLConnection(u) {
                            @Override
                            public void connect() {

                            }

                            @Override
                            public InputStream getInputStream() {
                                return new ByteArrayInputStream(data);
                            }
                        };
                    }
                };
                urls.add(new URL(CONTEXT_URL_PROTOCOL, "", -1, contextRootPath + CONTEXT_PATH_SEPARATOR + key, handler));
            }
        }
        return urls;
    }

    protected void registerWatchFile(File watchDirectory, FileEventHandler eventHandler) throws IOException {
        watcher.registerWatcher(watchDirectory, eventHandler);
    }
    /**
     * Holder to split default context file
     */
    private static class DirContextType {
        private final String contextRoot;
        private final String baseNodePath;
        private final String keyNodePath;

        public DirContextType(String contextRoot, String nodePath, String keyNodePath) {
            this.contextRoot = contextRoot;
            this.baseNodePath = nodePath;
            this.keyNodePath = keyNodePath;
        }

        public String getContextRoot() {
            return contextRoot;
        }

        public String getBaseNodePath() {
            return baseNodePath;
        }

        public String getKeyNodePath() {
            return keyNodePath;
        }
    }

    /**
     * Directory change event Watcher
     */
    private static class ContextFileWatcher implements Runnable {
        private final WatchService service;
        private final Thread watchThread;
        private final Map<WatchKey, FileEventHandler> handlerRegistry = new HashMap<>();

        private ContextFileWatcher() throws IOException {
            this.service = FileSystems.getDefault().newWatchService();
            watchThread = new Thread(this);
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    WatchKey key = service.take();
                    if(key != null) {
                        List<WatchEvent<?>> events = key.pollEvents();
                        for (WatchEvent<?> event : events) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                Path path = (Path) event.context();
                                FileEventHandler handler = handlerRegistry.get(key);
                                handler.onModified(path);
                            }
                        }
                        if (!key.reset()) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException ignored) {

            }
        }

        public void start() {
            watchThread.start();
        }

        public void stop() throws IOException {
            watchThread.interrupt();
            if(service != null) {
                service.close();
            }
        }

        public void registerWatcher(File directory, FileEventHandler handler) throws IOException {
            WatchKey key = directory.toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
            handlerRegistry.put(key, handler);
        }
    }

    protected interface FileEventHandler {
        void onModified(Path filePath);
    }
}
