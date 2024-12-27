package io.pocat.platform.gateway.connector;

import io.pocat.gateway.connector.Exchange;

public interface ServerHandler {
    void handle(Exchange exchange);
}
