package com.kodebytes.acasado.consumer;

import com.kodebytes.acasado.dto.OrderEventDto;
import com.kodebytes.acasado.service.OrderEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final OrderEventService orderEventService;

    public OrderEventsConsumer(OrderEventService orderEventService) {
        this.orderEventService = orderEventService;
    }

    // Default BATCH ack mode — no Acknowledgment parameter needed
    // @KafkaListener(topics = "order-events")
    // public void onMessage(ConsumerRecord<Integer, OrderEventDto> consumerRecord) {
    //     log.info("ConsumerRecord : {}", consumerRecord);
    //     orderEventService.processEvent(consumerRecord);
    // }

    @KafkaListener(topics = "order-events")
    public void onMessage(ConsumerRecord<Integer, OrderEventDto> consumerRecord,
                          Acknowledgment acknowledgment) {
        log.info("ConsumerRecord : {}", consumerRecord);
        orderEventService.processEvent(consumerRecord);
        // Only acknowledge on success — on exception, DefaultErrorHandler takes over:
        // it retries with FixedBackOff, then persists to failure_record table on exhaustion.
        acknowledgment.acknowledge();
    }
}

