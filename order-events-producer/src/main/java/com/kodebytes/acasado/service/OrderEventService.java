package com.kodebytes.acasado.service;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.exception.OrderEventPublishException;
import com.kodebytes.acasado.producer.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final OrderEventProducer orderEventProducer;

    public OrderEventService(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    /**
     * Publishes a new transaction order event to Kafka asynchronously.
     */
    public CompletableFuture<OrderEvent> createOrderEvent(OrderEvent orderEvent) {

        log.debug("Creating order event: orderEventId={}, phoneId={}",
                orderEvent.orderId(),
                orderEvent.phone() != null ? orderEvent.phone().phoneId() : null);

        return orderEventProducer
                .sendOrderEvent(orderEvent)
//                .sendOrderEventsInSingleTransactionAsync(orderEvent)
                .thenApply(_ -> orderEvent)
                .exceptionally(ex -> {
                    throw new OrderEventPublishException(
                            "Failed to publish order event to Kafka", ex.getCause());
                });
    }

    /**
     * Publishes a new transaction updated order event to Kafka asynchronously.
     */
    public CompletableFuture<OrderEvent> updateOrderEvent(OrderEvent orderEvent) {

        log.debug("Updating order event: orderEventId={}, orderId={}",
                orderEvent.orderId(),
                orderEvent.phone() != null ? orderEvent.phone().phoneId() : null);

        return orderEventProducer
//                .sendOrderEvent(orderEvent)
                .sendOrderEventTransactional(orderEvent)
                .thenApply(_ -> orderEvent)
                .exceptionally(ex -> {
                    throw new OrderEventPublishException(
                            "Failed to publish order event to Kafka", ex.getCause());
                });
    }
}

