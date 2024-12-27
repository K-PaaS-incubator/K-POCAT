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

import io.pocat.common.context.DirEnvContextProvider;
import io.pocat.env.ContextProvider;
import io.pocat.env.ContextProviderFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implements of {@link ContextProviderFactory} for service group
 */
public class ContainerContextProviderFactory implements ContextProviderFactory {
    /**
     * Config home property name
     */
    public static final String CONFIG_HOME_PROP_NAME = "io.pocat.container.config.home";
    /**
     * Service home property name
     */
    public static final String SERVICE_HOME_PROP_NAME = "io.pocat.container.services.home";

    /**
     * Deploy context root
     */
    public static final String DEPLOY_CONTEXT_ROOT_PATH = "/env/deploy";

    /**
     * Create ContextProvider
     * @param env Environment variables which will be used to create ContextProvider
     * @return ServiceGroupCtxProvider instance
     * @throws IOException if it encounters a problem to create provider.
     */
    @Override
    public ContextProvider createProvider(Map<String, String> env) throws IOException {
        return new ContainerContextProvider(env);
    }

    /**
     * Implementation of {@link ContextProvider} for service group
     */
    private static class ContainerContextProvider extends DirEnvContextProvider {
        public static final String DEPLOY_BASE_NODE_PATH = "/deploys/deploy";
        public static final String DEPLOY_KEY_NODE_PATH = "name";

        private static final String PROPERTIES_FILE_NAME = "env.properties";
        private static final String CONTEXT_FILE_NAME = "context.xml";
        private static final String DEPLOY_FILE_NAME = "deploy.xml";

        private static final String SERVICE_HOME_CONTEXT_PATH = "/env/services";
        private static final String SERVICE_LIBS_DIR_NAME = "libs";

        /**
         * Constructor
         *
         * @param env Environment variables
         */
        private ContainerContextProvider(Map<String, String> env) throws IOException {
            try {
                init(env);
            } catch (Exception e) {
                close();
                throw e;
            }
        }

        /**
         * Initialize context tree
         * @throws IllegalArgumentException if context file, deploy file, service home is not valid.
         */
        protected void init() {
            // Validate home directory first to fast fail if invalid directory
            // validate config home directory
            String configHome = getEnv().get(CONFIG_HOME_PROP_NAME);
            validateDirectory(configHome, true, false);

            // validate service home directory
            String serviceHome = getEnv().get(SERVICE_HOME_PROP_NAME);
            validateDirectory(serviceHome, true, true);

            try {
                reloadProperties(configHome + File.separator + PROPERTIES_FILE_NAME);
            } catch (IllegalArgumentException ignored) {
                // No properties defined
            }

            reloadContext(configHome + File.separator + CONTEXT_FILE_NAME);
            reloadDeploy(configHome + File.separator + DEPLOY_FILE_NAME);

            getContextTree().addContext(new Context(getContextTree(), SERVICE_HOME_CONTEXT_PATH));
            reloadServices(serviceHome);
            try {
                registerWatchFile(new File(configHome), filePath -> {
                    switch (filePath.toFile().getName()) {
                        case DEPLOY_FILE_NAME:
                            reloadDeploy(filePath.toString());
                            break;
                        case CONTEXT_FILE_NAME:
                            reloadContext(filePath.toString());
                            break;
                        case PROPERTIES_FILE_NAME:
                            reloadProperties(filePath.toString());
                            break;
                    }
                });

                registerWatchFile(new File(serviceHome), filePath -> reloadServices(serviceHome));
            } catch (IOException e) {
                throw new IllegalArgumentException("Register watch failed");
            }
        }

        /**
         * Load deployments to tree
         *
         * @param deployFilePath xml deploy file path
         */
        private void reloadDeploy(String deployFilePath) {
            File deployFile = new File(deployFilePath);
            if (deployFile.exists()) {
                try {
                    updateTree(DEPLOY_CONTEXT_ROOT_PATH, splitDocument(deployFile, DEPLOY_CONTEXT_ROOT_PATH, DEPLOY_BASE_NODE_PATH, DEPLOY_KEY_NODE_PATH));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to load deploy file [" + deployFilePath + "]", e);
                }
            }
        }

        /**
         * Load service home to tree
         *
         * @param serviceHome service home directory path
         */
        private void reloadServices(String serviceHome) {
            // Find all service directory
            List<URL> serviceUrls = new ArrayList<>();
            File serviceHomeDir = new File(serviceHome);
            File[] serviceDirs = serviceHomeDir.listFiles(File::isDirectory);
            if (serviceDirs != null) {
                for (File serviceDir : serviceDirs) {
                    Path servicePath = serviceDir.toPath();
                    Path libs = Paths.get(servicePath.toAbsolutePath().toString(), SERVICE_LIBS_DIR_NAME);
                    File libDir = libs.toFile();
                    if (!libDir.exists() || libDir.isFile()) {
                        // Not a valid service directory
                        continue;
                    }

                    File[] serviceLibs = libs.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
                    if (serviceLibs != null) {
                        for (File serviceLib : serviceLibs) {
                            String ctxPath = SERVICE_HOME_CONTEXT_PATH + "/" + serviceDir.getName() + "/" + SERVICE_LIBS_DIR_NAME + "/" + serviceLib.getName();
                            try {
                                // Create url and add handler to handle url
                                URLStreamHandler handler = new URLStreamHandler() {
                                    @Override
                                    public URLConnection openConnection(URL u) {
                                        return new URLConnection(u) {
                                            @Override
                                            public void connect() {

                                            }

                                            @Override
                                            public InputStream getInputStream() throws IOException {
                                                return new FileInputStream(serviceLib);
                                            }
                                        };
                                    }
                                };
                                getContextTree().addContext(new Context(getContextTree(), ctxPath, serviceLib.toURI().toURL()));
                                //serviceUrls.add(new URL(CONTEXT_URL_PROTOCOL, "", -1, ctxPath, handler));
                            } catch (MalformedURLException ignored) {
                                // never happened
                            }
                        }
                    }
                }
            }
            //updateTree(SERVICE_HOME_CONTEXT_PATH, serviceUrls);
        }
    }
}
