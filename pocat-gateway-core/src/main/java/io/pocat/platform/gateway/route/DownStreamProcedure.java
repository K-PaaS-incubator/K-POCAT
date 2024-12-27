package io.pocat.platform.gateway.route;

import io.pocat.gateway.message.MessageDelivery;

public interface DownStreamProcedure {
    void call(MessageDelivery message);
}
