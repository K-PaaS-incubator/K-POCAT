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

package io.pocat.messagebus.rabbitmq;

import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import io.pocat.platform.messagebus.NamespaceContext;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PropertiesMessageBusContextProvider implements MessageBusContextProvider {
    private final Properties properties;

    public PropertiesMessageBusContextProvider(Properties properties) {
        this.properties = properties;
    }

    @Override
    public NamespaceContext getNamespaceContext(String namespaceName) {
        return new PropertiesNamespaceContext(this.properties, namespaceName);
    }

    @Override
    public EndpointContext getEndpointContext(String endpointName) {
        return new PropertiesEndpointContext(this.properties, endpointName);
    }

    public static class PropertiesNamespaceContext implements NamespaceContext {
        private final Properties properties;
        private final String name;

        public PropertiesNamespaceContext(Properties properties, String name) {
            this.properties = properties;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEndpointRef() {
            return properties.getProperty("namespace." + name + ".endpoint.ref");
        }

        @Override
        public EndpointContext getEndpointContext() {
            return null;
        }

        @Override
        public Set<String> getPropertyNames() {
            Set<String> propNames = new HashSet<>();
            for(String propName:properties.stringPropertyNames()) {
                if(propName.startsWith("namespace." + this.name)) {
                    propNames.add(propName.substring(("namespace." + this.name + ".").length()));
                }
            }
            return propNames;
        }

        @Override
        public String getProperty(String name) {
            return properties.getProperty("namespace." + this.name + "." + name);
        }
    }

    private static class PropertiesEndpointContext implements EndpointContext {
        private final Properties properties;
        private final String name;

        public PropertiesEndpointContext(Properties properties, String name) {
            this.properties = properties;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEndpointType() {
            return properties.getProperty("endpoint." + name + ".type");
        }

        @Override
        public Set<String> getPropertyNames() {
            Set<String> propNames = new HashSet<>();
            for(String propName:properties.stringPropertyNames()) {
                if(propName.startsWith("endpoint." + this.name)) {
                    propNames.add(propName.substring(("endpoint." + this.name + ".").length()));
                }
            }
            return propNames;
        }

        @Override
        public String getProperty(String name) {
            return properties.getProperty("endpoint." + this.name + "." + name);
        }
    }
}
