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

import io.pocat.platform.messagebus.MessageDeliveryHandler;

import java.util.Map;

public class MockChannel {
    private MessageDeliveryHandler handler;
    public void publish(String messageSource, Map<String, String> headers, byte[] payload) {
        handler.onDelivery(messageSource, headers, payload);
    }

    public void subscribe(MessageDeliveryHandler handler) {
        this.handler = handler;
    }
}
