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

package io.pocat.resources;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResourceManagerTest {
    @Test
    public void testResourceManager() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("test1", "1");
        params.put("test2", "2");
        MockResourceContextProvider provider = new MockResourceContextProvider();
        provider.registerResourceContext(new MockResourceContext("/test/mock", params));

        ResourceManager manager = new ResourceManager(provider);
        MockResource resource = (MockResource) manager.getResource("/test/mock");
        assertEquals("1", resource.getParam("test1"));
        assertEquals("2", resource.getParam("test2"));
    }
}
