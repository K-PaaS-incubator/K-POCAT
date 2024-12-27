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

package io.pocat.common.context.messagebus;

import io.pocat.common.context.XmlContextUtil;
import io.pocat.env.ContextProvider;
import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import io.pocat.platform.messagebus.NamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link MessageBusContextProvider} using {@link ContextProvider}
 */
public class EnvMessageBusContextProvider implements MessageBusContextProvider {
    public static final String MESSAGEBUS_CONTEXT_ROOT = "/env/context/messagebus";
    public static final String NAMESPACE_CONTEXT_ROOT = MESSAGEBUS_CONTEXT_ROOT + "/namespaces";
    public static final String ENDPOINT_CONTEXT_ROOT = MESSAGEBUS_CONTEXT_ROOT + "/endpoints";

    private static final String NAMESPACE_NAME_NODE_PATH = "name";
    private static final String NAMESPACE_ENDPOINT_REF_NODE_PATH = "endpoint-ref";
    private static final String NAMESPACE_ENDPOINT_CONTEXT_NODE_PATH = "endpoint";
    private static final String NAMESPACE_PROP_NODE_PATH = "properties/property";

    private static final String ENDPOINT_NAME_NODE_PATH = "name";
    private static final String ENDPOINT_TYPE_NODE_PATH = "type";
    private static final String ENDPOINT_PROP_NODE_PATH = "properties/property";

    private static final String PROP_NAME_PATH = "@name";
    private static final String PROP_VALUE_PATH = "@value";

    private final ContextProvider provider;

    /**
     * Constructor
     * @param provider ContextProvider to get context
     */
    public EnvMessageBusContextProvider(ContextProvider provider) {
        this.provider = provider;
    }

    /**
     * Find namespace context
     * @param namespaceName namespace to find
     * @return found namespace context
     * @throws IOException if namespace context not exist or not readable
     */
    @Override
    public NamespaceContext getNamespaceContext(String namespaceName) throws IOException {
        if(provider.hasData(NAMESPACE_CONTEXT_ROOT + "/" + namespaceName)) {
            // todo add schema validator
            return buildNamespaceContext(namespaceName, XmlContextUtil.parseDocument(provider.openDataStream(NAMESPACE_CONTEXT_ROOT + "/" + namespaceName)));
        }
        throw new IOException("Namespace [" + namespaceName + "] not found");
    }

    /**
     * Find endpoint context
     * @param endpointName endpoint to find
     * @return found endpoint context
     * @throws IOException if endpoint context not exist or not readable
     */
    @Override
    public EndpointContext getEndpointContext(String endpointName) throws IOException {
        if(provider.hasData(ENDPOINT_CONTEXT_ROOT + "/" + endpointName)) {
            // todo add schema validator
            return buildEndpointContext(endpointName, XmlContextUtil.parseDocument(provider.openDataStream(ENDPOINT_CONTEXT_ROOT + "/" + endpointName)));
        }
        throw new IOException("Namespace [" + endpointName + "] not found");
    }

    /**
     * Build namespace context object from xml element root node
     * @param namespaceName namespace to create context
     * @param root xml root node
     * @return namespace context
     * @throws IOException if failed to parse xml document
     */
    private NamespaceContext buildNamespaceContext(String namespaceName, Node root) throws IOException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Map<String, String> properties = new HashMap<>();

            // parse with xpath
            String name = (String) xPath.evaluate(NAMESPACE_NAME_NODE_PATH, root, XPathConstants.STRING);
            if(name.isEmpty()) {
                throw new IOException("Namespace name element is empty");
            }

            String endpointRef = (String) xPath.evaluate(NAMESPACE_ENDPOINT_REF_NODE_PATH, root, XPathConstants.STRING);
            if(endpointRef.isEmpty()) {
                endpointRef = null;
            }

            Node endpointNode = (Node) xPath.evaluate(NAMESPACE_ENDPOINT_CONTEXT_NODE_PATH, root, XPathConstants.NODE);
            EndpointContext endpointContext = null;
            if(endpointNode != null) {
                endpointContext = buildEndpointContext(namespaceName, endpointNode);
            }

            NodeList propNodes = (NodeList) xPath.evaluate(NAMESPACE_PROP_NODE_PATH, root, XPathConstants.NODESET);
            if(propNodes != null) {
                for (int i = 0; i < propNodes.getLength(); i++) {
                    Node propNode = propNodes.item(i);
                    String propName = (String) xPath.evaluate(PROP_NAME_PATH, propNode, XPathConstants.STRING);
                    String propValue = (String) xPath.evaluate(PROP_VALUE_PATH, propNode, XPathConstants.STRING);
                    properties.put(propName, propValue);
                }
            }
            return new NamespaceContextImpl(name, endpointRef, endpointContext, properties);
        } catch (Exception e) {
            throw new IOException("Failed to create namespace [" + namespaceName + "].", e);
        }
    }

    private EndpointContext buildEndpointContext(String endpointName, Node root) throws IOException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Map<String, String> properties = new HashMap<>();
            // parse with xpath
            String name = (String) xPath.evaluate(ENDPOINT_NAME_NODE_PATH, root, XPathConstants.STRING);
            String type = (String) xPath.evaluate(ENDPOINT_TYPE_NODE_PATH, root, XPathConstants.STRING);

            NodeList propNodes = (NodeList) xPath.evaluate(ENDPOINT_PROP_NODE_PATH, root, XPathConstants.NODESET);
            if(propNodes != null) {
                for (int i = 0; i < propNodes.getLength(); i++) {
                    Node propNode = propNodes.item(i);
                    String propName = (String) xPath.evaluate(PROP_NAME_PATH, propNode, XPathConstants.STRING);
                    String propValue = (String) xPath.evaluate(PROP_VALUE_PATH, propNode, XPathConstants.STRING);
                    properties.put(propName, propValue);
                }
            }
            return new EndpointContextImpl(name, type, properties);
        } catch (Exception e) {
            throw new IOException("Failed to create endpoint [" + endpointName + "].", e);
        }
    }

    private static class NamespaceContextImpl implements NamespaceContext {
        private final String name;
        private final String endpointRef;
        private final EndpointContext endpointContext;
        private final Map<String, String> properties;

        private NamespaceContextImpl(String name, String endpointRef, EndpointContext endpointContext, Map<String, String> properties) {
            this.name = name;
            this.endpointRef = endpointRef;
            this.endpointContext = endpointContext;
            this.properties = properties;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEndpointRef() {
            return endpointRef;
        }

        @Override
        public EndpointContext getEndpointContext() {
            return endpointContext;
        }

        @Override
        public Set<String> getPropertyNames() {
            return Collections.unmodifiableSet(properties.keySet());
        }

        @Override
        public String getProperty(String propName) {
            return properties.get(propName);
        }
    }

    private static class EndpointContextImpl implements EndpointContext {
        private final String name;
        private final String type;
        private final Map<String, String> properties;

        private EndpointContextImpl(String name, String type, Map<String, String> properties) {
            this.name = name;
            this.type = type;
            this.properties = properties;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEndpointType() {
            return type;
        }

        @Override
        public Set<String> getPropertyNames() {
            return Collections.unmodifiableSet(properties.keySet());
        }

        @Override
        public String getProperty(String propName) {
            return properties.get(propName);
        }
    }
}
