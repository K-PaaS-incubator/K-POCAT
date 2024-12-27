package io.pocat.platform.gateway.route;

import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.route.RouteProcessException;

public interface RouteErrorProcedure {
    void call(Exchange exchange, RouteProcessException e);
}
