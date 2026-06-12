package com.kodebytes.acasado.service;

import com.kodebytes.acasado.domain.OrderEventDto;
import com.kodebytes.acasado.domain.OrderEventType;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.entity.OrderEvent;
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

    public void processEvent(ConsumerRecord<Integer, OrderEventDto> consumerRecord) {
        OrderEventDto orderEventDto = consumerRecord.value();
        log.info("OrderEventDto : {}", orderEventDto);
        try {
            validateDto(orderEventDto);
            validateConditionalRules(orderEventDto);

            if (orderEventDto.eventType() == OrderEventType.ADD) {
                OrderEvent orderEvent = orderEventMapper.toEntity(orderEventDto);
                OrderEvent saved = orderEventRepository.save(orderEvent);
                log.info("Persisted ADD event. orderEventId={}", saved.getOrderEventId());
                return;
            }

            if (orderEventDto.eventType() == OrderEventType.UPDATE) {
                Integer orderEventId = toOrderEventId(orderEventDto.orderEventId());
                OrderEvent existing = orderEventRepository.findById(orderEventId)
                        .orElseThrow(() -> new IllegalArgumentException("OrderEvent not found for update. orderEventId=" + orderEventId));

                orderEventMapper.updateEntity(orderEventDto, existing);
                OrderEvent updated = orderEventRepository.save(existing);
                log.info("Persisted UPDATE event. orderEventId={}", updated.getOrderEventId());
                return;
            }

            throw new IllegalArgumentException("Unsupported eventType: " + orderEventDto.eventType());

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

    public Optional<OrderEventResponseDto> findById(Integer orderEventId) {
        log.info("Fetching order event with id: {}", orderEventId);
        return orderEventRepository.findById(orderEventId)
                .map(orderEventMapper::toOrderEventResponseDto);
    }

    private void validateDto(OrderEventDto dto) {
        Set<ConstraintViolation<OrderEventDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.error("Bean validation failed for order event. dto={}, errors={}", dto, message);
            throw new IllegalArgumentException("Validation failed: " + message);
        }
    }

    private void validateConditionalRules(OrderEventDto dto) {
        if (dto.phone() == null) {
            log.error("Conditional validation failed: phone is null. dto={}", dto);
            throw new IllegalArgumentException("phone is required");
        }
        if (dto.eventType() == OrderEventType.UPDATE && dto.orderEventId() == null) {
            log.error("Conditional validation failed: UPDATE event missing orderEventId. dto={}", dto);
            throw new IllegalArgumentException("orderEventId is required for UPDATE event");
        }
    }

    private Integer toOrderEventId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("orderEventId is required");
        }
        return Math.toIntExact(id);
    }
}

