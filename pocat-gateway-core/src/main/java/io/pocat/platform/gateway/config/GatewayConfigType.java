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

package io.pocat.platform.gateway.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GatewayConfigType {
    private String name;
    private int workerPoolSize = Runtime.getRuntime().availableProcessors() * 16;
    private String responseNamespace;

    private AccessLoggerType accessLogger = null;
    private final List<ServiceConfigType> services = new ArrayList<>();

    public static GatewayConfigBuilder newBuilder() {
        return new GatewayConfigBuilder();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorkerPoolSize() {
        return this.workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    public String getResponseNamespace() {
        return this.responseNamespace;
    }

    public void setResponseNamespace(String responseNamespace) {
        this.responseNamespace = responseNamespace;
    }

    public AccessLoggerType getAccessLogger() {
        return accessLogger;
    }

    public void setAccessLogger(AccessLoggerType accessLogger) {
        this.accessLogger = accessLogger;
    }

    public List<ServiceConfigType> getServices() {
        return this.services;
    }

    public void addService(ServiceConfigType service) {
        this.services.add(service);
    }

    public static class GatewayConfigBuilder {
        private XPath xPath;
        public GatewayConfigType build(InputStream configStream) {
            try {
                GatewayConfigType config = new GatewayConfigType();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                Document document = documentBuilder.parse(configStream);

                Element root = document.getDocumentElement();
                root.normalize();
                xPath = XPathFactory.newInstance().newXPath();

                String name = ((String) xPath.evaluate("/gateway/name", root, XPathConstants.STRING)).trim();
                if(name.isBlank()) {
                    throw new IllegalArgumentException("Gateway name is empty");
                }
                config.setName(name);

                String responseNamespace = ((String) xPath.evaluate("/gateway/response-namespace", root, XPathConstants.STRING)).trim();
                if(responseNamespace.isBlank()) {
                    throw new IllegalArgumentException("Response namespace is empty");
                }
                config.setResponseNamespace(responseNamespace);

                String workPoolSize = ((String) xPath.evaluate("/gateway/worker-pool-size", root, XPathConstants.STRING)).trim();
                if(!workPoolSize.isBlank()) {
                    try {
                        config.setWorkerPoolSize(Integer.parseInt(workPoolSize));
                    } catch (NumberFormatException ignored) {
                        // If invalid use default value
                    }
                }
                config.setAccessLogger(buildAccessLogger(root));
                NodeList serviceNodes = (NodeList) xPath.evaluate("/gateway/services/service", root, XPathConstants.NODESET);
                if(serviceNodes != null) {
                    for (int i = 0; i < serviceNodes.getLength(); i++) {
                        Node serviceNode = serviceNodes.item(i);
                        config.addService(buildService(serviceNode));
                    }
                }

                return config;
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                throw new IllegalArgumentException("Invalid gateway config", e);
            }
        }

        private ServiceConfigType buildService(Node serviceNode) throws XPathExpressionException {
            ServiceConfigType serviceConfig = new ServiceConfigType();
            String name = (String) xPath.evaluate("name", serviceNode, XPathConstants.STRING);
            if(name == null || name.isBlank()) {
                throw new IllegalArgumentException("Service name is empty");
            }
            serviceConfig.setName(name);

            String protocol = (String) xPath.evaluate("protocol", serviceNode, XPathConstants.STRING);
            if(protocol == null || protocol.isBlank()) {
                throw new IllegalArgumentException("Service [" + name + "] has no protocol.");
            }
            serviceConfig.setProtocol(protocol);
            String routeGroup = (String) xPath.evaluate("route-group", serviceNode, XPathConstants.STRING);
            if(routeGroup == null || routeGroup.isBlank()) {
                throw new IllegalArgumentException("Service [" + name + "] has no route group.");
            }
            serviceConfig.setRouteGroup(routeGroup);

            NodeList connectorNodes = (NodeList) xPath.evaluate("connectors/connector", serviceNode, XPathConstants.NODESET);
            if(connectorNodes != null) {
                for(int i = 0; i < connectorNodes.getLength(); i++) {
                    serviceConfig.addConnectors(buildConnectorType(connectorNodes.item(i)));
                }
            }

            NodeList paramNodes = (NodeList) xPath.evaluate("service-params/service-param", serviceNode, XPathConstants.NODESET);
            if(paramNodes != null) {
                for(int j = 0; j < paramNodes.getLength(); j++) {
                    NameValueType param = new NameValueType();
                    Node paramNode = paramNodes.item(j);
                    param.setName(paramNode.getAttributes().getNamedItem("name").getNodeValue());
                    param.setValue(paramNode.getAttributes().getNamedItem("value").getNodeValue());
                    serviceConfig.addServiceParams(param);
                }
            }

            return serviceConfig;
        }

        private ConnectorConfigType buildConnectorType(Node connectorNode) throws XPathExpressionException {
            ConnectorConfigType connectorConfig = new ConnectorConfigType();
            String name = (String) xPath.evaluate("name", connectorNode, XPathConstants.STRING);
            if(name == null || name.isBlank()) {
                throw new IllegalArgumentException("Connector name is empty");
            }
            connectorConfig.setName(name);
            String port = (String) xPath.evaluate("port", connectorNode, XPathConstants.STRING);
            if(name.isBlank()) {
                throw new IllegalArgumentException("Connector [" + name + "] has not port.");
            }
            try {
                connectorConfig.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number [" + port + "]");
            }

            if(connectorNode.getAttributes().getNamedItem("acceptor") != null) {
                try {
                    connectorConfig.setAcceptor(Integer.parseInt(connectorNode.getAttributes().getNamedItem("acceptor").getNodeValue()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid acceptor number [" + connectorNode.getAttributes().getNamedItem("acceptor").getNodeValue() + "]");
                }
            }

            if(connectorNode.getAttributes().getNamedItem("selector") != null) {
                try {
                    connectorConfig.setAcceptor(Integer.parseInt(connectorNode.getAttributes().getNamedItem("selector").getNodeValue()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid selector number [" + connectorNode.getAttributes().getNamedItem("selector").getNodeValue() + "]");
                }
            }

            Node tlsConfigNode = (Node) xPath.evaluate("tls-config", connectorNode, XPathConstants.NODE);
            if(tlsConfigNode != null) {
                TLSConfigType tlsConfig = new TLSConfigType();
                tlsConfig.setCertPath((String) xPath.evaluate("cert-path", tlsConfigNode, XPathConstants.STRING));
                tlsConfig.setKeyPath((String) xPath.evaluate("key-path", tlsConfigNode, XPathConstants.STRING));
                tlsConfig.setKeyPassword((String) xPath.evaluate("key-password", tlsConfigNode, XPathConstants.STRING));

                connectorConfig.setTlsConfig(tlsConfig);
            }

            NodeList optionNodes = (NodeList) xPath.evaluate("options/option", connectorNode, XPathConstants.NODESET);
            if(optionNodes != null) {
                for(int j = 0; j < optionNodes.getLength(); j++) {
                    NameValueType option = new NameValueType();
                    Node paramNode = optionNodes.item(j);
                    option.setName(paramNode.getAttributes().getNamedItem("name").getNodeValue());
                    option.setValue(paramNode.getAttributes().getNamedItem("value").getNodeValue());
                    connectorConfig.setConnectorOption(option);
                }
            }

            return connectorConfig;
        }

        private AccessLoggerType buildAccessLogger(Node root) throws XPathExpressionException {
            AccessLoggerType accessLoggerType = new AccessLoggerType();
            NodeList accessLogHandlerNodes = (NodeList) xPath.evaluate("/gateway/access-logger/handler", root, XPathConstants.NODESET);
            if(accessLogHandlerNodes != null) {

                for(int i = 0; i < accessLogHandlerNodes.getLength(); i++) {
                    LogHandlerType handler = new LogHandlerType();
                    Node accessLogHandlerNode = accessLogHandlerNodes.item(i);
                    String type = (String) xPath.evaluate("type", accessLogHandlerNode, XPathConstants.STRING);
                    if(type != null && !type.isBlank()) {
                        handler.setType(type);
                        NodeList paramNodes = (NodeList) xPath.evaluate("params/param", accessLogHandlerNode, XPathConstants.NODESET);
                        if(paramNodes != null) {
                            for(int j = 0; j < paramNodes.getLength(); j++) {
                                NameValueType param = new NameValueType();
                                Node paramNode = paramNodes.item(j);
                                param.setName(paramNode.getAttributes().getNamedItem("name").getNodeValue());
                                param.setValue(paramNode.getAttributes().getNamedItem("value").getNodeValue());
                                handler.addParam(param);
                            }
                        }
                    }
                    accessLoggerType.addHandler(handler);
                }
            }
            return accessLoggerType;
        }
    }
}
