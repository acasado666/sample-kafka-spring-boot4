package com.kodebytes.acasado.domain;

import com.kodebytes.acasado.dto.OrderEventDto;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.dto.PhoneDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import  com.kodebytes.acasado.dto.OrderEventType;

public class OrderEventMapper {

    private OrderEventMapper() {
    }

    public static OrderEvent toEntity(OrderEventDto dto) {
        Phone phone = toPhoneEntity(dto.phone());
        OrderEvent orderEvent = new OrderEvent(dto.orderId(), dto.eventType(), phone);
        if (phone != null) {
            phone.setOrderEvent(orderEvent);
        }
        return orderEvent;
    }

    public static Phone toPhoneEntity(PhoneDto dto) {
        return new Phone(
                dto.phoneId(),
                dto.phoneName(),
                dto.phoneModel(),
                dto.phonePrice(),
                dto.phoneManufacturer()
        );
    }

    public static PhoneResponseDto toPhoneResponseDto(Phone phone) {
        Long orderEventId = phone.getOrderEvent() != null
                ? phone.getOrderEvent().getOrderId()
                : null;
        return new PhoneResponseDto(
                phone.getPhoneId(),
                phone.getPhoneName(),
                phone.getPhoneModel(),
                phone.getPhoneManufacturer(),
                phone.getPhonePrice(),
                orderEventId,
                phone.getCreatedAt(),
                phone.getUpdatedAt()
        );
    }

    public static OrderEventResponseDto toOrderEventResponseDto(OrderEvent orderEvent) {
        PhoneResponseDto phoneResponseDto = orderEvent.getPhone() != null
                ? toPhoneResponseDto(orderEvent.getPhone())
                : null;

        OrderEventType eventType = orderEvent.getEventType() != null
                ? OrderEventType.valueOf(orderEvent.getEventType().name())
                : null;

        return new OrderEventResponseDto(
                orderEvent.getOrderId(),
                eventType,
                phoneResponseDto,
                orderEvent.getCreatedAt(),
                orderEvent.getUpdatedAt()
        );
    }
}
