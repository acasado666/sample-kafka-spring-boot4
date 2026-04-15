package com.kodebytes.acasado.exception;

/**
 * Thrown when the Kafka producer fails to publish a {@code OrderEvent}.
 */
public class OrderEventException extends RuntimeException {

    public OrderEventException(String message, Throwable cause) {
        super(message, cause);
    }
}

