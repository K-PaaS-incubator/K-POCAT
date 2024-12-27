package io.pocat.platform.gateway.route;

import io.pocat.gateway.message.MessageDelivery;
import io.pocat.gateway.connector.Exchange;
import io.pocat.gateway.message.MessageHeaders;
import io.pocat.gateway.route.MessageConverter;
import io.pocat.gateway.route.RouteProcessException;
import io.pocat.platform.messagebus.MessageBusConnection;
import io.pocat.platform.gateway.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.pocat.gateway.message.MessageHeaders.REPLY_TO_HEADER_NAME;
import static io.pocat.gateway.message.MessageHeaders.TX_ID_HEADER_NAME;

public class UpstreamTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamTask.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private static final String ERROR_CODE_NAME = "error.code";
    private static final String ERROR_MESSAGE_NAME = "error.message";
    private static final String HEADER_PREFIX = "header:";

    private RouteErrorProcedure errorProcedure;
    private MessageConverter messageConverter;
    private MessageBusConnection connection;
    private String destination;
    private String replyTo;

    public UpstreamTask(MessageBusConnection connection) {
        this.connection = connection;
    }

    public void doTask(Exchange exchange, RouteProcedureChain chain) {
        DownStreamProcedure downStreamProcedure = message -> {
            int statusCode;
            try {
                statusCode = Integer.parseInt(message.getHeaders().get(MessageConstants.STATUS_CODE_HEADER_NAME));
            } catch (NumberFormatException e) {
                statusCode = MessageConstants.UNKNOWN_ERROR;
            }
            if(statusCode != 0) {
                errorProcedure.call(exchange, new RouteProcessException(statusCode, new String(message.getPayload(), StandardCharsets.UTF_8)));
                return;
            }

            messageConverter.convertMessageToExchange(message, exchange);
            chain.doNext(exchange);
        };

        DownStreamProcedureRegistry.getInstance().register(exchange.getTxId(), downStreamProcedure);
        try {
            String destination = buildTopic(exchange);
            MessageDelivery delivery = messageConverter.convertExchangeToMessage(exchange);
            Map<String, String> headers = new HashMap<>(delivery.getHeaders());
            headers.put(REPLY_TO_HEADER_NAME, replyTo);
            headers.put(TX_ID_HEADER_NAME, exchange.getTxId());
            connection.publish(destination, headers, delivery.getPayload());
        } catch (Exception e) {
            LOGGER.error("Exception thrown during upstream", e);
            errorProcedure.call(exchange, new RouteProcessException(MessageConstants.UNKNOWN_ERROR, "UnknownError"));
        }
    }

    private String buildTopic(Exchange exchange) {
        String result = destination;
        Matcher matcher = VARIABLE_PATTERN.matcher(destination);
        while(matcher.find()) {
            if(matcher.group(1).startsWith(HEADER_PREFIX)) {
                String value = exchange.getRequestHeader(matcher.group(1).substring(HEADER_PREFIX.length()));
                if(value != null) {
                    result = result.replace(matcher.group(), value);
                }
            } else {
                if(exchange.getAttribute(matcher.group(1)) != null) {
                    String value = (String) exchange.getAttribute(matcher.group(1));
                    result = result.replace(matcher.group(), value);
                }
            }
        }
        return result;
    }

    public void setConnection(MessageBusConnection connection) {
        this.connection = connection;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public void setErrorProcedure(RouteErrorProcedure errorProcedure) {
        this.errorProcedure = errorProcedure;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    private static class MessageDeliveryImpl implements MessageDelivery {
        @Override
        public MessageHeaders getHeaders() {
            return null;
        }

        @Override
        public byte[] getPayload() {
            return new byte[0];
        }
    }
}

