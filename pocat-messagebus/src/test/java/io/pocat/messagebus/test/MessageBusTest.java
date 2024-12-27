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

package io.pocat.messagebus.test;

import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.messagebus.MessageBusConnectionFactory;
import io.pocat.platform.messagebus.NamespaceContext;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MessageBusTest {
    @Test
    public void testMessageBusConnection() throws IOException {
        MockMessageBusContextProvider context = new MockMessageBusContextProvider();
        context.addNamespaceContext(new NamespaceContext() {
            @Override
            public String getName() {
                return "mock";
            }

            @Override
            public String getEndpointRef() {
                return null;
            }

            @Override
            public EndpointContext getEndpointContext() {
                return new EndpointContext() {
                    @Override
                    public String getName() {
                        return "mock";
                    }

                    @Override
                    public String getEndpointType() {
                        return "mock";
                    }

                    @Override
                    public Set<String> getPropertyNames() {
                        return Collections.emptySet();
                    }

                    @Override
                    public String getProperty(String name) {
                        return null;
                    }
                };
            }

            @Override
            public Set<String> getPropertyNames() {
                return Collections.emptySet();
            }

            @Override
            public String getProperty(String name) {
                return null;
            }
        });

        MessageBusConnectionFactory cf = new MessageBusConnectionFactory(context);
        MessageBusConnection connection = cf.newConnection();

        Map<String, String> resultMap = new HashMap<>();

        connection.bind("mock", "mock:test1");
        connection.bind("mock", "mock:test2");
        connection.subscribe("mock", (msgSource, messageHeader, payload) -> {
            System.out.println("message from [" + msgSource + "]");
            resultMap.put(msgSource, new String(payload, StandardCharsets.UTF_8));
        });

        connection.publish("mock:test1", new HashMap<>(), "Hello".getBytes(StandardCharsets.UTF_8));
        connection.publish("mock:test2", new HashMap<>(), "Hi".getBytes(StandardCharsets.UTF_8));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(2, resultMap.size());
        assertEquals("Hello", resultMap.get("mock:test1"));
        assertEquals("Hi", resultMap.get("mock:test2"));

        connection.close();
    }
}
