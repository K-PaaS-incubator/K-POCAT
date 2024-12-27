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

package io.pocat.platform.gateway;

import io.pocat.gateway.message.MessageDelivery;
import io.pocat.gateway.message.MessageHeaders;
import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.gateway.route.DownStreamProcedure;
import io.pocat.platform.gateway.route.DownStreamProcedureRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static io.pocat.platform.gateway.MessageConstants.TX_ID_HEADER_NAME;

public class ResponseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);
    private final String gatewayId;
    private MessageBusConnection connection;
    private ExecutorService executor;

    public ResponseHandler(String gatewayId, MessageBusConnection connection) {
        this.gatewayId = gatewayId;
        this.connection = connection;
    }

    public void init(String channelName, ExecutorService executor) {
        this.executor = executor;
        try {
            connection.bind(this.gatewayId, channelName);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot connect messagebus", e);
        }
    }

    public void start() throws IOException {
        connection.subscribe(this.gatewayId, (msgSource, messageHeader, payload) -> executor.execute(() -> {
            DownStreamProcedure procedure = DownStreamProcedureRegistry.getInstance().unregister(messageHeader.get(TX_ID_HEADER_NAME));
            if(procedure == null) {
                LOGGER.warn("Response for txid [" + messageHeader.get(TX_ID_HEADER_NAME) + "] not exist. Already time-outed.");
            } else {
                MessageHeaders headers = new MessageHeaders(messageHeader);
                procedure.call(new MessageDelivery() {
                    @Override
                    public MessageHeaders getHeaders() {
                        return headers;
                    }

                    @Override
                    public byte[] getPayload() {
                        return payload;
                    }
                });
            }
        }));
    }

    public void stop() throws IOException {

    }
}
