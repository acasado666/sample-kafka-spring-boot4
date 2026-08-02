package com.kodebytes.acasado.producer;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.exception.OrderEventPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TRANSACTION_ORDER_NAME = "transaction";

    @Value("${spring.kafka.topic:order-events}")
    private String topic;

    private final KafkaTemplate<Integer, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<Integer, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Asynchronous
    public CompletableFuture<SendResult<Integer, OrderEvent>> sendOrderEvent(OrderEvent orderEvent) {

        Integer key = orderEvent.orderId();
        log.info("Sending OrderEvent to topic={}, key={}, eventStatus={}", topic, key, orderEvent.eventType());

        CompletableFuture<SendResult<Integer, OrderEvent>> future = kafkaTemplate.send(topic, key, orderEvent);

        return future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderEvent | topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);
            } else {
                var metadata = result.getRecordMetadata();
                log.info("Published OrderEvent | topic={}, partition={}, offset={}, key={}",
                        metadata.topic(), metadata.partition(), metadata.offset(), key);
            }
        });
    }

    // Synchronous
    public SendResult<Integer, OrderEvent> sendOrderEventSynchronous(OrderEvent orderEvent) {

        Integer key = orderEvent.orderId();
        log.info("Sending Event synchronously to topic={}, key={}, eventType={}",
                topic, key, orderEvent.eventType());

        try {
            SendResult<Integer, OrderEvent> result = kafkaTemplate.send(topic, key, orderEvent)
                    .get(3, TimeUnit.SECONDS);

            var metadata = result.getRecordMetadata();
            log.info("Published OrderEvent synchronously | topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);

            return result;
        } catch (ExecutionException ex) {
            log.error("Failed to publish Event synchronously | topic={}, key={}, error={}",
                    topic, key, ex.getMessage(), ex);
            throw new OrderEventPublishException("Failed to publish OrderEvent synchronously", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing OrderEvent synchronously | topic={}, key={}", topic, key);
            throw new OrderEventPublishException("Interrupted while publishing OrderEvent synchronously", ex);
        } catch (TimeoutException ex) {
            log.error("Timed out while publishing OrderEvent synchronously | topic={}, key={}", topic, key);
            throw new OrderEventPublishException("Timed out while publishing OrderEvent synchronously", ex);
        }
    }

    // Transactional Asynchronous
    @Transactional
    public CompletableFuture<SendResult<Integer, OrderEvent>> sendOrderEventTransactional(OrderEvent orderEvent) {

        Integer key = orderEvent.orderId();
        if (isTransactionOrder(orderEvent)) {
            log.info("Transaction scenario detected for key={}; sending same event 3 times before forcing failure", key);
            for (int i = 1; i <= 3; i++) {
                kafkaTemplate.send(topic, key, orderEvent);
                log.info("Transaction scenario send attempt {} completed for key={}", i, key);
            }
            throw new RuntimeException(
                    "Forced rollback after publishing the same OrderEvent 3 times for transaction scenario"
            );
        }

        log.info("Sending OrderEvent transactionally to topic={}, key={}, eventType={}", topic, key, orderEvent.eventType());

        CompletableFuture<SendResult<Integer, OrderEvent>> future = kafkaTemplate.send(topic, key, orderEvent);

        return future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderEvent transactionally | topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);
            } else {
                var metadata = result.getRecordMetadata();
                log.info("Published OrderEvent transactionally | topic={}, partition={}, offset={}, key={}",
                        metadata.topic(), metadata.partition(), metadata.offset(), key);
            }
        });
    }

    // Transactional Synchronous
    @Transactional
    public SendResult<Integer, OrderEvent> sendOrderEventSynchronousTransactional(OrderEvent orderEvent) {

        Integer key = orderEvent.orderId();

        if (isTransactionOrder(orderEvent)) {
            log.info("Transaction scenario detected for key={}; sending same event 3 times synchronously before forcing failure", key);
            try {
                for (int i = 1; i <= 3; i++) {
                    kafkaTemplate.send(topic, key, orderEvent).get(3, TimeUnit.SECONDS);
                    log.info("Transaction scenario synchronous send attempt {} completed for key={}", i, key);
                }
            } catch (ExecutionException ex) {
                throw new OrderEventPublishException("Failed during transaction scenario synchronous publish", ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new OrderEventPublishException("Interrupted during transaction scenario synchronous publish", ex);
            } catch (TimeoutException ex) {
                throw new OrderEventPublishException("Timed out during transaction scenario synchronous publish", ex);
            }
            throw new RuntimeException(
                    "Forced rollback after publishing the same OrderEvent 3 times for transaction scenario");
        }

        log.info("Sending OrderEvent synchronously and transactionally to topic={}, key={}, eventType={}",
                topic, key, orderEvent.eventType());

        try {
            SendResult<Integer, OrderEvent> result = kafkaTemplate.send(topic, key, orderEvent)
                    .get(3, TimeUnit.SECONDS);

            var metadata = result.getRecordMetadata();
            log.info("Published OrderEvent synchronously and transactionally | topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);

            return result;
        } catch (ExecutionException ex) {
            log.error("Failed to publish OrderEvent synchronously and transactionally | topic={}, key={}, error={}",
                    topic, key, ex.getMessage(), ex);
            throw new OrderEventPublishException("Failed to publish OrderEvent synchronously and transactionally", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing OrderEvent synchronously and transactionally | topic={}, key={}", topic, key);
            throw new OrderEventPublishException("Interrupted while publishing OrderEvent synchronously and transactionally", ex);
        } catch (TimeoutException ex) {
            log.error("Timed out while publishing OrderEvent synchronously and transactionally | topic={}, key={}", topic, key);
            throw new OrderEventPublishException("Timed out while publishing OrderEvent synchronously and transactionally", ex);
        } catch (Exception e) {
            log.error("Unexpected error while publishing OrderEvent synchronously and transactionally | topic={}, key={}, error={}",
                    topic, key, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private boolean isTransactionOrder(OrderEvent orderEvent) {
        return orderEvent != null
                && orderEvent.phone() != null
                && orderEvent.phone().phoneName() != null
                && TRANSACTION_ORDER_NAME.equalsIgnoreCase(orderEvent.phone().phoneName().trim());
    }

    // Single Transaction Asynchronous
    public CompletableFuture<Void> sendOrderEventsInSingleTransactionAsync(OrderEvent orderEvent) {
        Integer key = orderEvent.orderId();
        log.info("Sending the same OrderEvent 3 times asynchronously in a single Kafka transaction | topic={}, key={}, eventType={}",
                topic, key, orderEvent.eventType());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                kafkaTemplate.executeInTransaction(ops -> {
                    for (int i = 1; i <= 3; i++) {
                        ops.send(topic, key, orderEvent);
                        log.info("Async transactional send attempt {} completed for key={}", i, key);
                    }
                    return null;
                })
        );

        return future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderEvent transactionally (async) | topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);
            } else {
                log.info("Published the same OrderEvent 3 times asynchronously in a single Kafka transaction | topic={}, key={}, completionResult={}",
                        topic, key, result);
            }
        }).exceptionally(ex -> {
            throw new OrderEventPublishException("Failed to publish OrderEvent transactionally (async)", ex);
        });
    }

}

