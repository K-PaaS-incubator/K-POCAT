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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class MockContextProvider extends DirEnvContextProvider {
    /**
     * Constructor
     *
     * @throws IOException if failed to create file watcher
     * @param env environment variables
     */
    protected MockContextProvider(Map<String, String> env) throws IOException {
        init(env);
    }

    @Override
    protected void init() {
        String path;
        URL resourceUrl = MockContextProvider.class.getResource("/context.xml");
        if(resourceUrl != null) {
            try {
                path = new File(resourceUrl.toURI()).getAbsolutePath();
                reloadContext(path);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
