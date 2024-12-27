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

package io.pocat.service.deploy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DeploymentDescriptorBuilder {
    private static final String NAME_NODE_PATH = "/deploy/name";
    private static final String SERVICE_NAME_NODE_PATH = "/deploy/service-name";
    private static final String SERVICE_CLASS_NODE_PATH = "/deploy/service-class";
    private static final String MAX_WORKER_NODE_PATH = "/deploy/max-worker";

    private static final String INIT_PARAMS_NODE_PATH = "/deploy/init-params/init-param";

    private static final String CONSUMER_GROUP_NODE_PATH = "/deploy/listener/consumer-group";
    private static final String CONSUMER_GROUP_NAME_NODE_PATH = "name";
    private static final String CONSUMER_GROUP_NODE_TYPE = "tap";
    private static final String CONSUMER_GROUP_CHANNEL_NODE_PATH = "channels/channel";

    private static final String RESOURCE_NODE_PATH = "/deploy/resources/resource";
    private static final String OUTBOUND_CHANNEL_NODE_PATH = "/deploy/resources/channel";

    private static final String NAME_ATTR_NAME = "name";
    private static final String PARAM_VALUE_ATTR_NAME = "value";
    private static final String REF_NAME_ATTR_NAME = "ref-name";
    private static final String RESOURCE_NAME_ATTR_NAME = "resource-name";
    private static final String CHANNEL_NAME_ATTR_NAME = "channel-name";


    private final XPath xPath;

    public DeploymentDescriptorBuilder() {
        xPath = XPathFactory.newInstance().newXPath();
    }

    public DeploymentDescriptor buildFrom(URL url) throws IOException {
        try (InputStream is = url.openStream()){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);

            Element root = document.getDocumentElement();
            root.normalize();

            Transformer t = TransformerFactory.newInstance().newTransformer();
            // Add Xml header
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            String name = (String) xPath.evaluate(NAME_NODE_PATH, root, XPathConstants.STRING);
            if(name.isEmpty()) {
                throw new IOException("Invalid deployment descriptor. Name does not set.");
            }

            String serviceName = (String) xPath.evaluate(SERVICE_NAME_NODE_PATH, root, XPathConstants.STRING);
            if(serviceName.isEmpty()) {
                throw new IOException("Invalid deployment descriptor. Service name does not set.");
            }

            String serviceClass = (String) xPath.evaluate(SERVICE_CLASS_NODE_PATH, root, XPathConstants.STRING);
            if(serviceClass.isEmpty()) {
                throw new IOException("Invalid deployment descriptor. Service class does not set.");
            }

            DeploymentDescriptor descriptor = new DeploymentDescriptor(name, serviceName, serviceClass);

            String workerNumStr = (String) xPath.evaluate(MAX_WORKER_NODE_PATH, root, XPathConstants.STRING);
            if(workerNumStr != null && !workerNumStr.isEmpty()) {
                try {
                    descriptor.setMaxWorker(Integer.parseInt(workerNumStr));
                } catch (NumberFormatException e) {
                    throw new IOException("max-worker element is not a number. [" + workerNumStr + "]");
                }
            }

            addInitParams(descriptor, root);
            addConsumerGroup(descriptor, root);
            addResources(descriptor, root);
            addOutboundChannel(descriptor, root);
            return descriptor;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse deployment descriptor [" + url.getPath() + "]", e);
        }
    }

    private void addInitParams(DeploymentDescriptor descriptor, Element root) throws XPathExpressionException, IOException {
        NodeList initParamNodeList = (NodeList) xPath.evaluate(INIT_PARAMS_NODE_PATH, root, XPathConstants.NODESET);
        if(initParamNodeList != null) {
            for(int i = 0; i < initParamNodeList.getLength(); i++) {
                Node initParamNode = initParamNodeList.item(i);
                Node nameNode = initParamNode.getAttributes().getNamedItem(NAME_ATTR_NAME);
                if(nameNode == null) {
                    throw new IOException("Initial parameter does not have name");
                }
                String paramName = nameNode.getNodeValue();
                if(paramName == null || paramName.isEmpty()) {
                    throw new IOException("Initial parameter name is empty");
                }
                Node valueNode = initParamNode.getAttributes().getNamedItem(PARAM_VALUE_ATTR_NAME);
                if(valueNode == null) {
                    throw new IOException("Initial parameter does not have value attribute");
                }
                String paramValue = valueNode.getNodeValue();
                descriptor.addInitParam(paramName, paramValue);
            }
        }
    }

    private void addConsumerGroup(DeploymentDescriptor descriptor, Element root) throws XPathExpressionException, IOException {
        NodeList consumerGroupNodeList = (NodeList) xPath.evaluate(CONSUMER_GROUP_NODE_PATH, root, XPathConstants.NODESET);
        if(consumerGroupNodeList != null) {
            for(int i = 0; i < consumerGroupNodeList.getLength(); i++) {
                Node consumerGroupNode = consumerGroupNodeList.item(i);
                String consumerGroupName = (String) xPath.evaluate(CONSUMER_GROUP_NAME_NODE_PATH, consumerGroupNode, XPathConstants.STRING);
                if(consumerGroupName == null || consumerGroupName.isEmpty()) {
                    throw new IOException("Consumer group[" + i + "] does not have value.");
                }
                ConsumerGroupDescriptor consumerGroupDescriptor = new ConsumerGroupDescriptor(consumerGroupName);

                Node typeNode = consumerGroupNode.getAttributes().getNamedItem(CONSUMER_GROUP_NODE_TYPE);
                if(typeNode != null) {
                    if(Boolean.parseBoolean(typeNode.getNodeValue())) {
                        consumerGroupDescriptor.setTapType(true);
                    }
                }

                NodeList channelNodeList = (NodeList) xPath.evaluate(CONSUMER_GROUP_CHANNEL_NODE_PATH, consumerGroupNode, XPathConstants.NODESET);
                if(channelNodeList != null) {
                    for (int j = 0; j < channelNodeList.getLength(); j++) {
                        Node channelNode = channelNodeList.item(j);
                        Node channelNameNode = channelNode.getAttributes().getNamedItem(NAME_ATTR_NAME);
                        if(channelNameNode == null) {
                            throw new IOException("Consumer group [" + consumerGroupName + "] channel [" + j + "] does not have name attribute.");
                        }
                        String channelName = channelNameNode.getNodeValue();
                        if(channelName == null || channelName.isEmpty()) {
                            throw new IOException("Consumer group [" + consumerGroupName + "] channel [" + j + "] does not have name attribute.");
                        }
                        consumerGroupDescriptor.addChannelName(channelName);
                    }
                }
                descriptor.addConsumerGroupDescriptor(consumerGroupDescriptor);
            }
        }
    }

    private void addResources(DeploymentDescriptor descriptor, Element root) throws XPathExpressionException, IOException {
        NodeList resourceNodeList = (NodeList) xPath.evaluate(RESOURCE_NODE_PATH, root, XPathConstants.NODESET);
        if(resourceNodeList != null) {
            for(int i = 0; i <resourceNodeList.getLength(); i++) {
                Node resourceNode = resourceNodeList.item(i);
                Node refNameNode = resourceNode.getAttributes().getNamedItem(REF_NAME_ATTR_NAME);
                if(refNameNode == null) {
                    throw new IOException("Resource [" + i + "] does not have ref-name attribute.");
                }
                String refName = refNameNode.getNodeValue();
                if(refName == null || refName.isEmpty()) {
                    throw new IOException("Resource [" + i + "] does not have ref-name attribute.");
                }

                Node resourceNameNode = resourceNode.getAttributes().getNamedItem(RESOURCE_NAME_ATTR_NAME);
                if(resourceNameNode == null) {
                    throw new IOException("Resource [" + i + "] does not have resource-name attribute.");
                }
                String resourceName = resourceNameNode.getNodeValue();
                if(resourceName == null || resourceName.isEmpty()) {
                    throw new IOException("Resource [" + i + "] does not have resource-name attribute.");
                }
                descriptor.addResourceDescriptor(new ResourceDescriptor(refName, resourceName));
            }
        }
    }

    private void addOutboundChannel(DeploymentDescriptor descriptor, Element root) throws XPathExpressionException, IOException {
        NodeList channelNodeList = (NodeList) xPath.evaluate(OUTBOUND_CHANNEL_NODE_PATH, root, XPathConstants.NODESET);
        if(channelNodeList != null) {
            for(int i = 0; i <channelNodeList.getLength(); i++) {
                Node channelNode = channelNodeList.item(i);
                Node refNameNode = channelNode.getAttributes().getNamedItem(REF_NAME_ATTR_NAME);
                if(refNameNode == null) {
                    throw new IOException("Channel [" + i + "] does not have ref-name attribute.");
                }
                String refName = refNameNode.getNodeValue();
                if(refName == null || refName.isEmpty()) {
                    throw new IOException("Channel [" + i + "] does not have ref-name attribute.");
                }

                Node channelNameNode = channelNode.getAttributes().getNamedItem(CHANNEL_NAME_ATTR_NAME);
                if(channelNameNode == null) {
                    throw new IOException("Channel [" + i + "] does not have channel-name attribute.");
                }
                String channelName = channelNameNode.getNodeValue();
                if(channelName == null || channelName.isEmpty()) {
                    throw new IOException("Resource [" + i + "] does not have resource-name attribute.");
                }
                descriptor.addOutboundChannelDescriptor(new OutboundChannelDescriptor(refName, channelName));
            }
        }
    }
}
