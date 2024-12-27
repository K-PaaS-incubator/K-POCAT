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

package io.pocat.platform.gateway.utils.stage;

import java.util.Map;

public class StageManager {
    private final Map<String, Stage> stages;

    public StageManager(Map<String, Stage> stages) {
        this.stages = stages;
    }

    public Stage getExecutor(String stageName) {
        return stages.get(stageName);
    }

    public void start() {
        for(Stage stage:stages.values()) {
            stage.start();
        }
    }

    public void stop() {
        for(Stage stage:stages.values()) {
            stage.stop();
        }
    }
}
