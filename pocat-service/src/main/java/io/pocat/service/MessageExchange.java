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

import java.util.Set;

/**
 * This interface represents message format to serve.
 * If set outbound headers or payload, exchange will send reply message; otherwise reply message will not send.
 */
public interface MessageExchange {
    /**
     * Return message source name where exchange from
     * @return message source name
     */
    String getFrom();

    /**
     * Return set of inbound message header names
     * @return set of inbound message header names
     */
    Set<String> getRequestHeaderNames();

    /**
     * Return inbound message header value of specified header name
     * @param headerName header name to get value
     * @return inbound message header value of specified header name
     */
    String getRequestHeader(String headerName);

    /**
     * Return inbound message payload
     * @return inbound message payload
     */
    byte[] getRequestPayload();

    /**
     * Return set of outbound message header names
     * @return set of outbound message header names
     */
    Set<String> getReplyHeaderNames();

    /**
     * Return outbound message header value of specified header name
     * @param headerName header name to get value
     * @return outbound message header value of specified header name
     */
    String getReplyHeader(String headerName);

    /**
     * Set outbound message header with specified header name
     * If header is already exist, the header value will be overwritten
     * @param headerName header name to set
     * @param headerValue header value to set
     */
    void setReplyHeader(String headerName, String headerValue);

    /**
     * Return outbound message payload
     * @return outbound message payload
     */
    byte[] getReplyPayload();

    /**
     * Set outbound message payload
     * @param payload outbound message payload
     */
    void setReplyPayload(byte[] payload);
}
