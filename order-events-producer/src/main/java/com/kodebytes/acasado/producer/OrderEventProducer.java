package com.kodebytes.acasado.producer;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.exception.OrderEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes {@link com.kodebytes.acasado.domain.OrderEvent} messages to a Kafka topic.
 *
 * <p>Topic is read from {@code spring.kafka.topic} in {@code application.yml}.
 * The message key is {@code libraryEventId} so that events for the same
 * library record land on the same partition (ordering guarantee).
 *
 * <p>A {@code whenComplete} callback is attached to every send for
 * success/failure logging without blocking the calling thread.
 * The returned {@link CompletableFuture} can be blocked on by the
 * caller when a synchronous guarantee is required.
 *
 * <p><b>Serializer mode (JsonSerializer — active):</b>
 * {@code KafkaTemplate} serializes {@code LibraryEvent} automatically via Jackson's
 * {@code JsonSerializer}. To switch to {@code StringSerializer}, see the commented
 * code in this class and toggle {@code application.yml}.
 */
@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    @Value("${spring.kafka.topic}")
    private String topic;

    // JsonSerializer mode: KafkaTemplate carries the OrderEvent object directly
    private final KafkaTemplate<Long, OrderEvent> kafkaTemplate;
    // StringSerializer mode (switch): swap the line above with the one below
    // private final KafkaTemplate<Long, String> kafkaTemplate;

    // StringSerializer mode (switch): add ObjectMapper field below
    // private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<Long, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    // StringSerializer mode (switch): replace constructor above with:
    // public LibraryEventProducer(KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper) {
    //     this.kafkaTemplate = kafkaTemplate;
    //     this.objectMapper  = objectMapper;
    // }

    /**
     * Publishes {@code orderEvent} to the configured Kafka topic.
     *
     * @param orderEvent the event to publish; its {@code orderId} is used as the message key
     * @return a {@link CompletableFuture} that completes with the send result or
     *         exceptionally with a {@link OrderEventException}
     */
    public CompletableFuture<SendResult<Long, OrderEvent>> sendOrderEvent(OrderEvent orderEvent) {
        // StringSerializer mode (switch): change return type to CompletableFuture<SendResult<Long, String>>
        //                                 and replace kafkaTemplate.send(...) call below with the
        //                                 manual serialization block:
        //   String value;
        //   try {
        //       value = objectMapper.writeValueAsString(orderEvent);
        //   } catch (JsonProcessingException e) {
        //       throw new OrderEventException("Failed to serialize OrderEvent to JSON", e);
        //   }
        //   CompletableFuture<SendResult<Long, String>> future = kafkaTemplate.send(topic, key, value);

        Long key = orderEvent.orderId();

        log.info("Sending OrderEvent to topic={}, key={}, eventStatus={}", topic, key, orderEvent.eventType());

        CompletableFuture<SendResult<Long, OrderEvent>> future = kafkaTemplate.send(topic, key, orderEvent);

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

    /**
     * Publishes {@code orderEvent} to the configured Kafka topic <em>synchronously</em>.
     *
     * <p>Unlike {@link #sendOrderEvent(OrderEvent)} (OrderEvent)}, this method blocks the calling
     * thread until the broker acknowledgement is received (or a timeout/error occurs).
     * Use this when you need a guaranteed delivery confirmation before continuing.
     *
     * @param orderEvent the event to publish; its {@code libraryEventId} is used as the message key
     * @return the {@link SendResult} containing broker metadata for the published record
     * @throws OrderEventException if the send fails, times out, or the thread is interrupted
     */
    public SendResult<Long, OrderEvent> sendOrderEventSynchronous(OrderEvent orderEvent) {
        Long key = orderEvent.orderId();

        log.info("Sending LibraryEvent synchronously to topic={}, key={}, eventType={}",
                topic, key, orderEvent.eventType());

        try {
            SendResult<Long, OrderEvent> result = kafkaTemplate.send(topic, key, orderEvent)
                    .get(3, TimeUnit.SECONDS);

            var metadata = result.getRecordMetadata();
            log.info("Published OrderEvent synchronously | topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);

            return result;
        } catch (ExecutionException ex) {
            log.error("Failed to publish LibraryEvent synchronously | topic={}, key={}, error={}",
                    topic, key, ex.getMessage(), ex);
            throw new OrderEventException("Failed to publish OrderEvent synchronously", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing OrderEvent synchronously | topic={}, key={}", topic, key);
            throw new OrderEventException("Interrupted while publishing OrderEvent synchronously", ex);
        } catch (TimeoutException ex) {
            log.error("Timed out while publishing OrderEvent synchronously | topic={}, key={}", topic, key);
            throw new OrderEventException("Timed out while publishing OrderEvent synchronously", ex);
        }
    }
}

