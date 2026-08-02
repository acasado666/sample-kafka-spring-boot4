package com.kodebytes.acasado.service;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.domain.OrderEventType;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.entity.OrderEventDao;
import com.kodebytes.acasado.mapper.OrderEventMapper;
import com.kodebytes.acasado.repository.OrderEventRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderEventService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final OrderEventRepository orderEventRepository;
    private final OrderEventMapper orderEventMapper;
    private final Validator validator;


    public OrderEventService(OrderEventRepository orderEventRepository,
                             OrderEventMapper orderEventMapper,
                             Validator validator) {
        this.orderEventRepository = orderEventRepository;
        this.orderEventMapper = orderEventMapper;
        this.validator = validator;
    }

    public void processEvent(ConsumerRecord<Integer, OrderEvent> consumerRecord) {
        OrderEvent orderEvent = consumerRecord.value();
        var key = consumerRecord.key();
        log.info("orderEvent : {}", orderEvent);
        try {
            validateDto(orderEvent);
            validateConditionalRules(orderEvent);

            if (orderEvent.eventType() == OrderEventType.ADD) {
                OrderEventDao orderEventDao = orderEventMapper.toEntity(orderEvent);
                OrderEventDao saved = orderEventRepository.save(orderEventDao);
                log.info("Persisted ADD event. orderId={}", saved.getOrderId());
                return;
            }

            if (orderEvent.eventType() == OrderEventType.UPDATE) {
                Integer orderId = consumerRecord.key();
                OrderEventDao orderEventDao = orderEventMapper.toEntity(orderEvent);
                OrderEventDao existing = orderEventRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("OrderEvent not found for update. orderId=" + key));

                orderEventMapper.updateEntity(orderEventDao, existing);
                OrderEventDao updated = orderEventRepository.save(existing);
                log.info("Persisted UPDATE event. orderId={}", updated.getOrderId());
                return;
            }

            throw new IllegalArgumentException("Unsupported eventType: " + orderEvent.eventType());

        } catch (Exception e) {
            log.error("Error processing order event. consumerRecord={}, error={}", consumerRecord, e.getMessage(), e);
            throw e; // rethrow to trigger DefaultErrorHandler
        }
    }

    public List<OrderEventResponseDto> findAll() {
        log.info("Fetching all order events");
        return orderEventRepository.findAll()
                .stream()
                .map(orderEventMapper::toOrderEventResponseDto)
                .toList();
    }

    public Optional<OrderEventResponseDto> findById(Integer orderId) {
        log.info("Fetching order event with id: {}", orderId);
        return orderEventRepository.findById(orderId)
                .map(orderEventMapper::toOrderEventResponseDto);
    }

    private void validateDto(OrderEvent orderEvent) {
        Set<ConstraintViolation<OrderEvent>> violations = validator.validate(orderEvent);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.error("Bean validation failed for order event. orderEvent={}, errors={}", orderEvent, message);
            throw new IllegalArgumentException("Validation failed: " + message);
        }
    }

        private void validateConditionalRules(OrderEvent orderEvent) {
        if (orderEvent.phone() == null) {
            log.error("Conditional validation failed: phone is null. orderEvent={}", orderEvent);
            throw new IllegalArgumentException("phone is required");
        }
        if (orderEvent.eventType() == OrderEventType.UPDATE && orderEvent.orderId() == null) {
            log.error("Conditional validation failed: UPDATE event missing orderId. orderEvent={}", orderEvent);
            throw new IllegalArgumentException("orderId from OrderEvent is required for UPDATE event");
        }
    }
}

