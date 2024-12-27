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

package io.pocat.platform.gateway;

import io.pocat.common.context.DirEnvContextProvider;
import io.pocat.env.ContextProvider;
import io.pocat.env.ContextProviderFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class GatewayContextProviderFactory implements ContextProviderFactory {
    private static final String CONFIG_HOME_PROP_NAME = "io.pocat.gateway.config.home";
    private static final String ROUTES_HOME_PROP_NAME = "io.pocat.gateway.routes.home";

    private static final String PROPERTIES_FILE_NAME = "env.properties";
    private static final String CONTEXT_FILE_NAME = "context.xml";
    private static final String CONFIG_FILE_NAME = "gateway-config.xml";

    private static final String ROUTES_CONTEXT_ROOT_PATH = "/env/routes";
    private static final String ROUTES_HOME_DIR_NAME = "routes";

    @Override
    public ContextProvider createProvider(Map<String, String> env) throws IOException {
        return new GatewayContextProvider(env);
    }

    private static class GatewayContextProvider extends DirEnvContextProvider {
        public GatewayContextProvider(Map<String, String> env) throws IOException {
            try {
                init(env);
            } catch (Exception e) {
                close();
                throw e;
            }
        }

        @Override
        protected void init() {
            String configHome = getEnv().get(CONFIG_HOME_PROP_NAME);
            validateDirectory(configHome, true, false);

            // validate service home directory
            String routeHome = getEnv().get(ROUTES_HOME_PROP_NAME);
            validateDirectory(routeHome, true, true);

            try {
                reloadProperties(configHome + File.separator + PROPERTIES_FILE_NAME);
            } catch (IllegalArgumentException ignored) {
                // No properties defined
            }

            reloadContext(configHome + File.separator + CONTEXT_FILE_NAME);
            reloadConfig(configHome + File.separator + CONFIG_FILE_NAME);
            reloadRoutes(configHome + File.separator + ROUTES_HOME_DIR_NAME);
        }

        private void reloadRoutes(String routesDirPath) {
            File routesDir = new File(routesDirPath);
            File[] routeGroupsDir = routesDir.listFiles(File::isDirectory);
            if(routeGroupsDir != null ){
                for (File routeGroupDir : routeGroupsDir) {
                    File routeGroupCfgFile = new File(routeGroupDir.getAbsolutePath() + ".xml" );
                    if(!routeGroupCfgFile.exists()) {
                        continue;
                    }
                    String groupCtxPath = ROUTES_CONTEXT_ROOT_PATH + "/" + routeGroupDir.getName();
                    try {
                        getContextTree().addContext(new Context(getContextTree(), groupCtxPath, routeGroupCfgFile.toURI().toURL()));
                    } catch (MalformedURLException ignored) {

                    }
                    File[] routeCfgFiles = routeGroupDir.listFiles(pathname -> pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml"));
                    if(routeCfgFiles != null) {
                        for (File routeCfgFile : routeCfgFiles) {
                            try {
                                getContextTree().addContext(new Context(getContextTree(),
                                        groupCtxPath + "/" + routeCfgFile.getName().substring(0, routeCfgFile.getName().length() - ".xml".length()),
                                        routeCfgFile.toURI().toURL()));
                            } catch (MalformedURLException ignored) {

                            }
                        }
                    }
                }
            }
        }

        private void reloadConfig(String configFilePath) {
            File configFile = new File(configFilePath);
            try {
                getContextTree().addContext(new Context(getContextTree(), Gateway.GATEWAY_CONFIG_CONTEXT_PATH, configFile.toURI().toURL()));
            } catch (MalformedURLException ignored) {

            }
        }
    }
}
