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

import io.pocat.common.context.messagebus.EnvMessageBusContextProvider;
import io.pocat.common.context.resources.EnvResourceContextProvider;
import io.pocat.env.ContextProvider;
import io.pocat.platform.messagebus.EndpointContext;
import io.pocat.platform.messagebus.MessageBusContextProvider;
import io.pocat.platform.messagebus.NamespaceContext;
import io.pocat.resources.ResourceContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ModuleContextTest {
    private static final Map<String, String> env = new HashMap<>();
    @BeforeClass
    public static void buildEnv() {
        env.put("io.pocat.context.provider.factory", MockContextProviderFactory.class.getName());
    }

    @Test
    public void testMessageBusContext() throws Exception {
        ContextProvider provider = new MockContextProviderFactory().createProvider(env);
        MessageBusContextProvider mbcProvider = new EnvMessageBusContextProvider(provider);
        EndpointContext endpointContext = mbcProvider.getEndpointContext("rabbit01");
        assertEquals("rabbit01", endpointContext.getName());
        assertEquals("rabbitmq", endpointContext.getEndpointType());
        assertEquals(4, endpointContext.getPropertyNames().size());

        NamespaceContext alpha = mbcProvider.getNamespaceContext("alpha");
        assertEquals("rabbit01", alpha.getEndpointRef());
        assertNull(alpha.getEndpointContext());
        assertEquals(1, alpha.getPropertyNames().size());

        NamespaceContext beta = mbcProvider.getNamespaceContext("beta");
        assertEquals("rabbit03", beta.getEndpointContext().getName());
        assertNull(beta.getEndpointRef());
        assertEquals(1, beta.getPropertyNames().size());
        provider.close();
    }

    @Test
    public void testResourceContext() throws Exception {
        ContextProvider provider = new MockContextProviderFactory().createProvider(env);
        EnvResourceContextProvider resourceProvider = new EnvResourceContextProvider(provider);
        ResourceContext ctx = resourceProvider.getResourceContext("mongo/mongo02");
        assertEquals("mongo/mongo02", ctx.getResourcePath());
        assertEquals("mongodb", ctx.getResourceType());
        assertEquals(1, ctx.getProperties().size());

        provider.close();
    }
}
