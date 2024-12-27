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

package io.pocat.platform.launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class GatewayLauncher {
    private static final String DEFAULT_CONFIG_HOME = System.getProperty("user.dir") + File.separator + "config";
    private static final String DEFAULT_ROUTES_HOME = DEFAULT_CONFIG_HOME + File.separator + "routes";
    private static final String DEFAULT_LIBS_PATH = "/libs";

    private static final String CONFIG_HOME_PROP_NAME = "io.pocat.gateway.config.home";
    private static final String ROUTES_HOME_PROP_NAME = "io.pocat.gateway.routes.home";
    private static final String LIBS_PATH_PROP_NAME = "io.pocat.gateway.libs";
    private static final String CONTEXT_PROVIDER_FACTORY_PROP_NAME = "io.pocat.context.provider.factory";
    private static final String DEFAULT_CONTEXT_PROVIDER_FACTORY = "io.pocat.platform.gateway.GatewayContextProviderFactory";
    private static final String GATEWAY_BOOTSTRAP_CLASS_NAME = "io.pocat.platform.gateway.GatewayBootstrap";
    public static void main(String[] args) throws Exception {
        GatewayLauncher launcher = new GatewayLauncher();
        launcher.launch(args);
    }

    private void launch(String[] args) throws Exception {
        Map<String, String> env = new HashMap<>();
        String configHome = System.getProperty(CONFIG_HOME_PROP_NAME, DEFAULT_CONFIG_HOME);
        String routesHome = System.getProperty(ROUTES_HOME_PROP_NAME, DEFAULT_ROUTES_HOME);

        String libsPath = System.getProperty(LIBS_PATH_PROP_NAME, configHome + DEFAULT_LIBS_PATH);

        env.put(CONFIG_HOME_PROP_NAME, configHome);
        env.put(ROUTES_HOME_PROP_NAME, routesHome);
        env.put(LIBS_PATH_PROP_NAME, libsPath);
        env.put(CONTEXT_PROVIDER_FACTORY_PROP_NAME, System.getProperty(CONTEXT_PROVIDER_FACTORY_PROP_NAME, DEFAULT_CONTEXT_PROVIDER_FACTORY));

        File[] jarFiles = getFiles(libsPath);
        URL[] urls = new URL[jarFiles.length];
        for(int i = 0; i < jarFiles.length; i++) {
            urls[i] = jarFiles[i].toURI().toURL();
        }
        URLClassLoader bootstrapLoader = new URLClassLoader(urls);
        Thread.currentThread().setContextClassLoader(bootstrapLoader);
        Class<?> bootstrapClass = Class.forName(GATEWAY_BOOTSTRAP_CLASS_NAME, true, bootstrapLoader);
        Object bootstrap = bootstrapClass.getConstructor().newInstance();
        Method bootMethod = bootstrapClass.getMethod("boot", Map.class);
        bootMethod.invoke(bootstrap, env);
    }

    private File[] getFiles(String libsPath) {
        File libsDir = new File(libsPath);
        if((!libsDir.exists()) || libsDir.isFile()) {
            return new File[0];
            //throw new IllegalArgumentException("Library directory [" + libsDir.getAbsolutePath() + "] is not valid library directory");
        }
        File[] jarFiles = libsDir.listFiles((dir, name) -> (!dir.isFile()) && name.toLowerCase().endsWith(".jar"));
        if(jarFiles == null) {
            throw new IllegalArgumentException("Library files does not exist in directory [" + libsDir.getAbsolutePath() + "].");
        }
        return jarFiles;
    }
}
