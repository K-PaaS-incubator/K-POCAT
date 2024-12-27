package io.pocat.gateway.route.http;

import io.pocat.gateway.connector.http.HttpExchange;
import io.pocat.gateway.message.MessageDelivery;
import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.message.MessageHeaders;
import io.pocat.gateway.route.MessageConverter;

import java.util.HashMap;
import java.util.Map;

public class HttpMessageConverter implements MessageConverter {
    @Override
    public void convertMessageToExchange(MessageDelivery message, Exchange exchange) {
        // todo Move header to exchange
        for(Map.Entry<String, String> header:message.getHeaders().entrySet()) {
            exchange.setResponseHeader(header.getKey(), header.getValue());
        }
        exchange.setResponseContents(message.getPayload());
    }

    @Override
    public MessageDelivery convertExchangeToMessage(Exchange exchange) {
        if(!(exchange instanceof HttpExchange)) {
            throw new IllegalArgumentException("Not a http exchange");
        }
        HttpExchange httpExchange = (HttpExchange) exchange;
        Map<String, String> rawHeaders = new HashMap<>();
        for(String headerName:httpExchange.getRequestHeaderNames()) {
            rawHeaders.put(headerName, httpExchange.getRequestHeader(headerName));
        }
        for(String paramName:httpExchange.getRequestParamNames()) {
            rawHeaders.put(paramName, httpExchange.getRequestParam(paramName));
        }

        MessageHeaders headers = new MessageHeaders(rawHeaders);
        // todo Move header to message
        return new MessageDelivery() {
            @Override
            public MessageHeaders getHeaders() {
                return headers;
            }

            @Override
            public byte[] getPayload() {
                return httpExchange.getRequestContents();
            }
        };
    }
}
