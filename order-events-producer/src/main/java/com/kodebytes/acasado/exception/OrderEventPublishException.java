package com.kodebytes.acasado.exception;

/**
 * Thrown when the Kafka producer fails to publish a {@code OrderEvent}.
 */
public class OrderEventPublishException extends RuntimeException {

    public OrderEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}

