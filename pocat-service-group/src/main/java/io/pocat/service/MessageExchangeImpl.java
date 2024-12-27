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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageExchangeImpl implements MessageExchange {
    private boolean isSetReply = false;
    private final String fromChannelName;
    private final Map<String, String> requestHeaders;
    private final byte[] requestPayload;
    private final boolean isTap;
    private final Map<String, String> replyHeaders = new HashMap<>();
    private byte[] replyPayload = new byte[0];


    public MessageExchangeImpl(String fromChannelName, Map<String, String> requestHeaders, byte[] requestPayload, boolean isTap) {
        this.fromChannelName = fromChannelName;
        this.requestHeaders = requestHeaders;
        this.requestPayload = requestPayload;
        this.isTap = isTap;
    }

    @Override
    public String getFrom() {
        return this.fromChannelName;
    }

    @Override
    public Set<String> getRequestHeaderNames() {
        return Collections.unmodifiableSet(this.requestHeaders.keySet());
    }

    @Override
    public String getRequestHeader(String headerName) {
        return this.requestHeaders.get(headerName);
    }

    @Override
    public byte[] getRequestPayload() {
        return this.requestPayload;
    }

    @Override
    public Set<String> getReplyHeaderNames() {
        return Collections.unmodifiableSet(this.replyHeaders.keySet());
    }

    @Override
    public String getReplyHeader(String headerName) {
        return this.replyHeaders.get(headerName);
    }

    @Override
    public void setReplyHeader(String headerName, String headerValue) {
        this.isSetReply = true;
        this.replyHeaders.put(headerName, headerValue);
    }

    @Override
    public byte[] getReplyPayload() {
        return this.replyPayload;
    }

    @Override
    public void setReplyPayload(byte[] payload) {
        this.isSetReply = true;
        this.replyPayload = payload;
    }

    public boolean isSetReply() {
        return (!this.isTap)&&this.isSetReply;
    }

    public Map<String, String> getReplyHeaders() {
        return replyHeaders;
    }
}
