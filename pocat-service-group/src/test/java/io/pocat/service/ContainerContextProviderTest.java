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

import io.pocat.env.ContextProvider;
import io.pocat.env.EventType;
import io.pocat.env.InitialEnvContextProvider;
import io.pocat.service.deploy.DeploymentDescriptor;
import io.pocat.service.deploy.DeploymentDescriptorBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class ContainerContextProviderTest {
    private static final Map<String, String> env = new HashMap<>();
    @BeforeClass
    public static void buildEnv() throws IOException {
        Properties properties = new Properties();
        properties.load(ContainerContextProviderTest.class.getResourceAsStream("/test.prop"));
        env.put(ContainerContextProviderFactory.CONFIG_HOME_PROP_NAME, properties.getProperty("resources.config"));
        env.put(ContainerContextProviderFactory.SERVICE_HOME_PROP_NAME, properties.getProperty("resources.service"));
        env.put("io.pocat.context.provider.factory", ContainerContextProviderFactory.class.getName());
    }

    @Test
    public void testDeploymentDescriptor() throws IOException {
        ContextProvider provider = new InitialEnvContextProvider(env);
        DeploymentDescriptor descriptor = new DeploymentDescriptorBuilder().buildFrom(provider.getDataURL(ContainerContextProviderFactory.DEPLOY_CONTEXT_ROOT_PATH + "/" + "deployName"));
        System.out.println(descriptor);
    }

    @Test
    public void testInit() throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        ContextProvider provider = new InitialEnvContextProvider(env);

        provider.watchContext("/env/services", (type, key) -> {
            try {
                System.out.println("Event!" + type.name() + ":" + key);
                if(type.equals(EventType.CREATE)) {
                    latch.countDown();
                    assertEquals(2, provider.listChildNames("/env/services").size());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        assertEquals("TestValue", provider.getProperty("io.pocat.test"));
        InputStream is = provider.openDataStream("/env/context/resources/mongo01");
        System.out.println(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        assertEquals(1, provider.listChildNames("/env/services").size());
        System.out.println("OK");

        String serviceHome = env.get(ContainerContextProviderFactory.SERVICE_HOME_PROP_NAME);
        createSample2Dir(serviceHome);

        try {
            latch.await();
            provider.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!(new File(serviceHome + File.separator + "sample2").delete())) {
            System.out.println("Not deleted");
        }
    }

    private void createSample2Dir(String serviceHome) throws IOException {
        File sample2Dir = new File(serviceHome + File.separator + "sample2");
        if(!sample2Dir.mkdirs()) {
            throw new IOException("Failed to create service dir");
        }
        sample2Dir.deleteOnExit();
        File libDir = new File(sample2Dir, "libs");
        if(!libDir.mkdirs()) {
            throw new IOException("Failed to create service lib dir");
        }
        libDir.deleteOnExit();
        File fromFile = new File(serviceHome + File.separator + "sample" + File.separator + "libs" + File.separator + "test.jar");
        File toFile = new File(serviceHome + File.separator + "sample2" + File.separator + "libs" + File.separator + "test.jar");
        Files.copy(fromFile.toPath(), toFile.toPath());
        toFile.deleteOnExit();
    }
}
