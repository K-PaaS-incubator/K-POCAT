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

package io.pocat.common.context.resources;

import io.pocat.env.ContextProvider;
import io.pocat.resources.ResourceContext;
import io.pocat.resources.ResourceContextProvider;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.pocat.common.context.XmlContextUtil.parseDocument;

public class EnvResourceContextProvider implements ResourceContextProvider {
    public static final String RESOURCE_CONTEXT_ROOT = "/env/context/resources";

    private static final String RESOURCE_NAME_NODE_PATH = "name";
    private static final String RESOURCE_TYPE_NODE_PATH = "type";
    private static final String RESOURCE_PROP_NODE_PATH = "properties/property";

    private static final String PROP_NAME_PATH = "@name";
    private static final String PROP_VALUE_PATH = "@value";

    private final ContextProvider provider;

    public EnvResourceContextProvider(ContextProvider provider) {
        this.provider = provider;
    }

    @Override
    public ResourceContext getResourceContext(String resourcePath) throws IOException {
        if(resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        if(provider.hasData(RESOURCE_CONTEXT_ROOT + "/" + resourcePath)) {
            // todo add schema validator
            return buildResourceContext(resourcePath, parseDocument(provider.openDataStream(RESOURCE_CONTEXT_ROOT + "/" + resourcePath)));
        }
        throw new IOException("Resource [" + resourcePath + "] not found");
    }

    private ResourceContext buildResourceContext(String resourcePath, Node root) throws IOException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Map<String, String> properties = new HashMap<>();

            // parse with xpath
            String name = (String) xPath.evaluate(RESOURCE_NAME_NODE_PATH, root, XPathConstants.STRING);
            String type = (String) xPath.evaluate(RESOURCE_TYPE_NODE_PATH, root, XPathConstants.STRING);

            NodeList propNodes = (NodeList) xPath.evaluate(RESOURCE_PROP_NODE_PATH, root, XPathConstants.NODESET);
            if(propNodes != null) {
                for (int i = 0; i < propNodes.getLength(); i++) {
                    Node propNode = propNodes.item(i);
                    String propName = (String) xPath.evaluate(PROP_NAME_PATH, propNode, XPathConstants.STRING);
                    String propValue = (String) xPath.evaluate(PROP_VALUE_PATH, propNode, XPathConstants.STRING);
                    properties.put(propName, propValue);
                }
            }
            return new ResourceContextImpl(name, type, properties);
        } catch (Exception e) {
            throw new IOException("Failed to create resource context [" + resourcePath + "]", e);
        }
    }

    private static class ResourceContextImpl implements ResourceContext {
        private final String resourcePath;
        private final String resourceType;
        private final Map<String, String> properties;

        private ResourceContextImpl(String resourcePath, String resourceType, Map<String, String> properties) {
            this.resourcePath = resourcePath;
            this.resourceType = resourceType;
            this.properties = properties;
        }

        @Override
        public String getResourcePath() {
            return this.resourcePath;
        }

        @Override
        public String getResourceType() {
            return this.resourceType;
        }

        @Override
        public Map<String, String> getProperties() {
            return this.properties;
        }
    }
}
