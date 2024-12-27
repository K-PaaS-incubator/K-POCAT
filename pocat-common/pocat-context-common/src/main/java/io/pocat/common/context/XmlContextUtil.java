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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to be used by module context providers
 */
public final class XmlContextUtil {
    /**
     * Utility class. Cannot create instance.
     */
    private XmlContextUtil() {

    }

    /**
     * Parse xml from inputstream and find root element node
     * @param is inputstream contains xml
     * @return xml root node
     * @throws IOException if parser configuration failed or invalid xml document
     */
    public static Node parseDocument(InputStream is) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);
            document.getDocumentElement().normalize();
            return document.getDocumentElement();
        } catch (ParserConfigurationException e) {
            throw new IOException("Invalid parser configuration", e);
        } catch (SAXException e) {
            throw new IOException("Invalid xml", e);
        }
    }
}
