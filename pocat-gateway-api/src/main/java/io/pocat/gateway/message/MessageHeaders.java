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

package io.pocat.gateway.message;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MessageHeaders implements Map<String, String> {
    public static final String TX_ID_HEADER_NAME = "Tx-Id";
    public static final String MESSAGE_ID_HEADER_NAME = "Message-Id";
    public static final String CORRELATION_ID_HEADER_NAME = "Correlation-Id";

    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    public static final String REPLY_TO_HEADER_NAME = "Reply-To";
    public static final String REPLY_TOPIC_HEADER_NAME = "Reply-Topic";

    public static final String STATUS_CODE_HEADER_NAME = "Status-Code";

    private final Map<String, String> rawHeaders;

    public MessageHeaders(Map<String, String> rawHeaders) {
        this.rawHeaders = rawHeaders;
    }

    public String getTxId() {
        return (String) rawHeaders.get(TX_ID_HEADER_NAME);
    }

    public String getMessageId() {
        return (String) rawHeaders.get(MESSAGE_ID_HEADER_NAME);
    }

    public String getCorrelationId() {
        return (String) rawHeaders.get(CORRELATION_ID_HEADER_NAME);
    }

    public String getContentType() {
        return (String) rawHeaders.get(CONTENT_TYPE_HEADER_NAME);
    }

    public Object getReplyTo() {
        return rawHeaders.get(REPLY_TO_HEADER_NAME);
    }

    public String getReplyTopic() {
        return (String) rawHeaders.get(REPLY_TOPIC_HEADER_NAME);
    }

    @Override
    public int size() {
        return rawHeaders.size();
    }

    @Override
    public boolean isEmpty() {
        return rawHeaders.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return rawHeaders.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return rawHeaders.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return rawHeaders.get(key);
    }

    @Override
    public String put(String key, String value) {
        return rawHeaders.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return rawHeaders.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        rawHeaders.putAll(m);
    }

    @Override
    public void clear() {
        rawHeaders.clear();
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(rawHeaders.keySet());
    }

    @Override
    public Collection<String> values() {
        return Collections.unmodifiableCollection(rawHeaders.values());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(rawHeaders.entrySet());
    }
}
