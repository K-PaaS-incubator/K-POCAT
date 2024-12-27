package io.pocat.platform.gateway.route;

import io.pocat.gateway.connector.Exchange;

public interface RouteProcedureChain {
    void doNext(Exchange exchange);
}
