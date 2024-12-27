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

package io.pocat.env;

/**
 * This interface represents context or property changing event handler.
 */
public interface EventWatcher {
    /**
     * Call if watching target changing event occurred
     * @param type {@link EventType}
     * @param key key that represent event occurred target.
     *            Context path if target is context
     *            and property name if target is property
     */
    void handleEvent(EventType type, String key);
}
