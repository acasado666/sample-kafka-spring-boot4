package com.kodebytes.acasado.service;

import com.kodebytes.acasado.domain.OrderEvent;
import com.kodebytes.acasado.domain.OrderEventMapper;
import com.kodebytes.acasado.domain.OrderEventType;
import com.kodebytes.acasado.domain.Phone;
import com.kodebytes.acasado.dto.OrderEventDto;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.repository.OrderEventRepository;
import com.kodebytes.acasado.repository.PhoneRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderEventService {

    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final OrderEventRepository orderEventRepository;
    private final PhoneRepository phoneRepository;

    public OrderEventService(OrderEventRepository orderEventRepository,
                             PhoneRepository phoneRepository) {
        this.orderEventRepository = orderEventRepository;
        this.phoneRepository = phoneRepository;
    }

    @Transactional
    public void processEvent(ConsumerRecord<Integer, OrderEventDto> consumerRecord) {
        OrderEventDto orderEventDto = consumerRecord.value();
        log.info("OrderEventDto : {}", orderEventDto);

        if (orderEventDto.eventType() == OrderEventType.UPDATE) {
            validate(orderEventDto);
        }

        save(orderEventDto);
    }

    private void validate(OrderEventDto orderEventDto) {
        if (orderEventDto.orderId() == null) {
            throw new IllegalArgumentException("Order Event Id is missing");
        }

        Optional<OrderEvent> orderEventOptional = orderEventRepository.findById(orderEventDto.orderId());
        if (orderEventOptional.isEmpty()) {
            throw new IllegalArgumentException("Not a valid order Event");
        }
        log.info("Validation is successful for the order Event : {}", orderEventOptional.get());
    }

    private void save(OrderEventDto orderEventDto) {
        OrderEvent orderEvent = OrderEventMapper.toEntity(orderEventDto);

        // For updates, we need the original to keep its createdAt timestamp if managed by @PrePersist
        // but JPA's @PrePersist handles it if it's a new entity.
        // If it's an update, the ID is already set in the entity from the mapper.

        // Save OrderEvent first — it has @GeneratedValue(IDENTITY), DB generates the ID for new ones
        orderEvent.setPhone(null); // detach phone temporarily to avoid cascade issues on persist
        OrderEvent savedOrderEvent = orderEventRepository.save(orderEvent);

        // Now save Phone with the FK pointing to the persisted OrderEvent
        Phone phone = OrderEventMapper.toPhoneEntity(orderEventDto.phone());
        phone.setOrderEvent(savedOrderEvent);
        Phone savedPhone = phoneRepository.save(phone);

        // Set bidirectional back-reference for in-memory consistency
        savedOrderEvent.setPhone(savedPhone);

        log.info("Successfully persisted/updated the phone event : {}", savedPhone);
    }

    public List<OrderEventResponseDto> findAll() {
        log.info("Fetching all orders events");
        return orderEventRepository.findAll()
                .stream()
                .map(OrderEventMapper::toOrderEventResponseDto)
                .toList();
    }

    public Optional<OrderEventResponseDto> findById(Long orderEventId) {
        log.info("Fetching order event with id: {}", orderEventId);
        return orderEventRepository.findById(orderEventId)
                .map(OrderEventMapper::toOrderEventResponseDto);
    }
}

