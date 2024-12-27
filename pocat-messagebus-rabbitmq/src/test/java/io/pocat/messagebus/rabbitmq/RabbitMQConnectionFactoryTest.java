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

import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.messagebus.MessageBusConnectionFactory;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class RabbitMQConnectionFactoryTest {
    @Test
    public void testRabbitMQ() throws IOException {
        CountDownLatch latch = new CountDownLatch(2);
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/context.properties"));
        MessageBusContextProvider ctx = new PropertiesMessageBusContextProvider(properties);

        MessageBusConnectionFactory cf = new MessageBusConnectionFactory(ctx);
        MessageBusConnection connection = cf.newConnection();

        Map<String, String> resultMap = new HashMap<>();

        connection.bind("test", "rabbit1:test1");
        connection.bind("test", "rabbit2:test2");
        connection.subscribe("test", (msgSource, messageHeader, payload) -> {
            System.out.println("message from [" + msgSource + "]");
            System.out.println(messageHeader);
            resultMap.put(msgSource, new String(payload, StandardCharsets.UTF_8));
            latch.countDown();
        });

        Map<String, String> headers = new HashMap<>();
        headers.put("Name", "PoCAT");
        headers.put("value", "1");
        connection.publish("rabbit1:test1", headers, "Hello".getBytes(StandardCharsets.UTF_8));
        connection.publish("rabbit2:test2", new HashMap<>(), "Hi".getBytes(StandardCharsets.UTF_8));

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2, resultMap.size());
        assertEquals("Hello", resultMap.get("rabbit1:test1"));
        assertEquals("Hi", resultMap.get("rabbit2:test2"));

        connection.close();
    }
}
