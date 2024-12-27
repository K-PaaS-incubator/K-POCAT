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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class ServiceClassLoader extends URLClassLoader {
    private final ClassLoader systemClassLoader;

    public ServiceClassLoader(URL[] urls) {
        this(urls, null);
    }

    public ServiceClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        systemClassLoader = getSystemClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if(c == null) {
                try {
                    if (systemClassLoader != null) {
                        c = systemClassLoader.loadClass(name);
                    }
                } catch (ClassNotFoundException ignored) {
                    // Not exist class in system class loader
                }
            }
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // Not exist class in this class loader
            }

            if( c == null) {
                // if not exist in parent, throw ClassNotFoundException
                c = super.loadClass(name, resolve);
            }

            if(resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = new ArrayList<>();
        if(systemClassLoader != null) {
            Enumeration<URL> systemResources = systemClassLoader.getResources(name);
            if(systemResources != null) {
                while(systemResources.hasMoreElements()) {
                    resources.add(systemResources.nextElement());
                }
            }
        }

        Enumeration<URL> currentResources = findResources(name);
        if(currentResources != null) {
            while(currentResources.hasMoreElements()) {
                resources.add(currentResources.nextElement());
            }
        }

        Enumeration<URL> parentResources = super.getResources(name);
        if(parentResources != null) {
            while(parentResources.hasMoreElements()) {
                resources.add(parentResources.nextElement());
            }
        }

        return new Enumeration<>() {
            private final Iterator<URL> iterator = resources.iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public URL getResource(String name) {
        URL resourceUrl = null;
        if(systemClassLoader != null) {
            resourceUrl = systemClassLoader.getResource(name);
        }

        if(resourceUrl == null) {
            resourceUrl = findResource(name);
        }

        if(resourceUrl == null) {
            resourceUrl = super.getResource(name);
        }

        return resourceUrl;
    }
}
