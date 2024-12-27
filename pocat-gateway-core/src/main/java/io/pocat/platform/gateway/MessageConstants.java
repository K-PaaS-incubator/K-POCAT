package io.pocat.platform.gateway;

public class MessageConstants {
    public static final int SUCCESS_CODE = 0;
    public static final int BAD_REQUEST = 40000;
    public static final int NOT_AUTHORIZED = 40100;
    public static final int FORBIDDEN = 40300;
    public static final int NOT_FOUND = 40400;
    public static final int NOT_ACCEPTABLE = 40600;
    public static final int PRECONDITION_FAILED = 41200;
    public static final int TOO_LARGE_CONTENTS = 41300;
    public static final int TOO_MANY_REQUEST = 42900;
    public static final int UNKNOWN_ERROR = 50000;
    public static final int BAD_GATEWAY = 50200;
    public static final int SERVICE_UNAVAILABLE = 50300;
    public static final int GATEWAY_TIMEOUT = 50400;

    public static final String TX_ID_HEADER_NAME = "Tx-Id";
    public static final String MESSAGE_ID_HEADER_NAME = "Message-Id";
    public static final String CORRELATION_ID_HEADER_NAME = "Correlation-Id";

    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    public static final String REPLY_TO_HEADER_NAME = "Reply-To";
    public static final String REPLY_TOPIC_HEADER_NAME = "Reply-Topic";

    public static final String STATUS_CODE_HEADER_NAME = "Status-Code";

    private MessageConstants() {

    }
}
