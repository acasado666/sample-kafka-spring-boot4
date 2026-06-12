package com.kodebytes.acasado.mapper;

import com.kodebytes.acasado.domain.OrderEventDto;
import com.kodebytes.acasado.dto.PhoneDto;
import com.kodebytes.acasado.dto.PhoneResponseDto;
import com.kodebytes.acasado.dto.OrderEventResponseDto;
import com.kodebytes.acasado.entity.Phone;
import com.kodebytes.acasado.entity.OrderEvent;
import org.springframework.stereotype.Component;

@Component
public class OrderEventMapper {

    public OrderEvent toEntity(OrderEventDto dto) {
        validatePhone(dto);

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setEventType(dto.eventType());

        Phone phone = mapPhone(dto);
        orderEvent.setPhone(phone);

        return orderEvent;
    }

    public void updateEntity(OrderEventDto dto, OrderEvent existing) {
        validatePhone(dto);

        existing.setEventType(dto.eventType());

        Phone existingPhone = existing.getPhone();
        if (existingPhone == null) {
            existingPhone = new Phone();
        }

        existingPhone.setPhoneId(toPhoneId(dto.phone().phoneId()));
        existingPhone.setPhoneName(dto.phone().phoneName());
        existingPhone.setPhoneModel(dto.phone().phoneModel());
        existingPhone.setPhoneManufacturer(dto.phone().phoneManufacturer());
        existingPhone.setPhonePrice(dto.phone().phonePrice());
        existing.setPhone(existingPhone);
    }

    public Phone toPhoneEntity(PhoneDto dto) {
        Phone phone = new Phone();
        phone.setPhoneId(dto.phoneId());
        phone.setPhoneName(dto.phoneName());
        phone.setPhoneModel(dto.phoneModel());
        phone.setPhoneManufacturer(dto.phoneManufacturer());
        phone.setPhonePrice(dto.phonePrice());
        return phone;
    }

    public PhoneResponseDto toPhoneResponseDto(Phone phone) {
        Integer orderEventId = phone.getOrderEvent() != null
                ? phone.getOrderEvent().getOrderEventId()
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

    public OrderEventResponseDto toOrderEventResponseDto(OrderEvent orderEvent) {
        PhoneResponseDto phone = orderEvent.getPhone() != null
                ? toPhoneResponseDto(orderEvent.getPhone())
                : null;
        return new OrderEventResponseDto(
                orderEvent.getOrderEventId(),
                orderEvent.getEventType(),
                phone,
                orderEvent.getCreatedAt(),
                orderEvent.getUpdatedAt()
        );
    }

    private Phone mapPhone(OrderEventDto dto) {
        Phone phone = new Phone();
        Long id = dto.phone().phoneId();
        phone.setPhoneId(toPhoneId(id));
        phone.setPhoneName(dto.phone().phoneName());
        phone.setPhoneModel(dto.phone().phoneModel());
        phone.setPhoneManufacturer(dto.phone().phoneManufacturer());
        phone.setPhonePrice(dto.phone().phonePrice());
        return phone;
    }

    private void validatePhone(OrderEventDto dto) {
        if (dto.phone() == null) {
            throw new IllegalArgumentException("phone is required");
        }
    }

    private Integer toPhoneId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("phone.phoneId is required");
        }
        return Math.toIntExact(id);
    }
}
