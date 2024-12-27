package io.pocat.gateway.route;

import io.pocat.gateway.message.MessageDelivery;
import io.pocat.gateway.connector.Exchange;

public interface MessageConverter {
    void convertMessageToExchange(MessageDelivery message, Exchange exchange);
    MessageDelivery convertExchangeToMessage(Exchange exchange);
}
